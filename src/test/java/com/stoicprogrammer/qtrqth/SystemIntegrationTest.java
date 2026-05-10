package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator;
import com.stoicprogrammer.qtrqth.serial.SerialConnector;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Business Rule: [SYSTEM INTEGRITY] - End-to-End Operational Flow.
 */
class SystemIntegrationTest extends BddTest {

    private final SystemFixture fixture = new SystemFixture();

    @Test
    void givenSimulatedHardware_whenDataFlows_thenFinalGpsDataIsProduced() throws Exception {
        fixture.givenSimulatedHardwareReady();
        
        // Feed multiple sentences to build a full record
        fixture.whenSerialDataArrives("$GPRMC,123456,A,4000.000,N,08000.000,W,0,0,010126,,,A\r\n");
        fixture.whenSerialDataArrives("$GPGGA,123456,4000.000,N,08000.000,W,1,08,1.0,100.0,M,,M,,\r\n");
        
        fixture.thenCalculatedLatitudeIs(40.0);
        fixture.thenCalculatedLongitudeIs(-80.0);
        fixture.thenCalculatedAltitudeIs(100.0);
        fixture.thenSatelliteCountIs(8);
        fixture.thenCalculatedTimeIs(java.time.LocalTime.of(12, 34, 56));
    }

    private class SystemFixture {
        private final ISerialProvider mockProvider = mock(ISerialProvider.class);
        private final ISerialPort mockPort = mock(ISerialPort.class);
        private final ConfigManager mockConfig = mock(ConfigManager.class);
        private final NmeaSentenceAccumulator accumulator = new NmeaSentenceAccumulator();
        private final NmeaParser parser = new NmeaParser();
        private final SerialConnector connector = new SerialConnector(mockConfig, accumulator, mockProvider);
        
        private com.fazecast.jSerialComm.SerialPortDataListener capturedListener;
        private final List<GpsData> results = new ArrayList<>();
        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        void givenSimulatedHardwareReady() {
            given(mockConfig.getProperty("serial.baud")).willReturn("9600");
            given(mockProvider.getPort("SIM1")).willReturn(mockPort);
            given(mockPort.openPort()).willReturn(true);
            
            AtomicReference<GpsData> state = new AtomicReference<>(new GpsData(null, null, 0, 0, 0, 0));
            
            // Connect and start pipeline in background thread
            java.util.stream.Stream<String> stream = connector.connect("SIM1");
            executor.submit(() -> {
                stream.map(s -> state.updateAndGet(fix -> parser.parse(s, fix)))
                      .forEach(results::add);
            });

            ArgumentCaptor<com.fazecast.jSerialComm.SerialPortDataListener> captor = ArgumentCaptor.forClass(com.fazecast.jSerialComm.SerialPortDataListener.class);
            verify(mockPort).addDataListener(captor.capture());
            capturedListener = captor.getValue();
        }

        void whenSerialDataArrives(String raw) {
            byte[] bytes = raw.getBytes();
            given(mockPort.bytesAvailable()).willReturn(bytes.length);
            given(mockPort.readBytes(any(byte[].class), anyInt())).willAnswer(inv -> {
                byte[] buffer = inv.getArgument(0);
                System.arraycopy(bytes, 0, buffer, 0, bytes.length);
                return bytes.length;
            });

            com.fazecast.jSerialComm.SerialPortEvent event = mock(com.fazecast.jSerialComm.SerialPortEvent.class);
            given(event.getEventType()).willReturn(com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE);
            capturedListener.serialEvent(event);
        }

        void thenCalculatedLatitudeIs(double expected) throws Exception {
            waitForResults();
            then(results.get(results.size()-1).latitude(), expected);
        }

        void thenCalculatedLongitudeIs(double expected) throws Exception {
            waitForResults();
            then(results.get(results.size()-1).longitude(), expected);
        }

        void thenCalculatedAltitudeIs(double expected) throws Exception {
            waitForResults();
            then(results.get(results.size()-1).altitude(), expected);
        }

        void thenSatelliteCountIs(int expected) throws Exception {
            waitForResults();
            then(results.get(results.size()-1).satelliteCount(), expected);
        }

        void thenCalculatedTimeIs(java.time.LocalTime expected) throws Exception {
            waitForResults();
            then(results.get(results.size()-1).utcTime(), expected);
        }

        private void waitForResults() throws Exception {
            long start = System.currentTimeMillis();
            while (results.isEmpty() && System.currentTimeMillis() - start < 2000) {
                Thread.sleep(10);
            }
            executor.shutdownNow();
        }
    }
}
