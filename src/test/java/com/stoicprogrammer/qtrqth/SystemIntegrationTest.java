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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

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
        private final List<GpsData> results = new CopyOnWriteArrayList<>();
        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        void givenSimulatedHardwareReady() {
            given(mockConfig.getProperty("serial.baud")).willReturn(Optional.of("9600"));
            given(mockProvider.getPort("SIM1")).willReturn(mockPort);
            given(mockPort.openPort()).willReturn(true);
            
            final AtomicReference<GpsData> state = new AtomicReference<>(new GpsData(null, null, 0, 0, 0, 0));
            
            // Connect and start pipeline in background thread
            final Stream<String> stream = connector.connect("SIM1");
            executor.submit(() -> stream.map(s -> state.updateAndGet(fix -> parser.parse(s, fix)))
                                        .forEach(results::add));

            final ArgumentCaptor<com.fazecast.jSerialComm.SerialPortDataListener> captor = ArgumentCaptor.forClass(com.fazecast.jSerialComm.SerialPortDataListener.class);
            verify(mockPort).addDataListener(captor.capture());
            capturedListener = captor.getValue();
        }

        void whenSerialDataArrives(final String raw) {
            final byte[] bytes = raw.getBytes();
            given(mockPort.bytesAvailable()).willReturn(bytes.length);
            given(mockPort.readBytes(any(byte[].class), anyInt())).willAnswer(inv -> {
                final byte[] buffer = inv.getArgument(0);
                System.arraycopy(bytes, 0, buffer, 0, bytes.length);
                return bytes.length;
            });

            final com.fazecast.jSerialComm.SerialPortEvent event = mock(com.fazecast.jSerialComm.SerialPortEvent.class);
            given(event.getEventType()).willReturn(com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE);
            capturedListener.serialEvent(event);
        }

        void thenCalculatedLatitudeIs(final double expected) throws Exception {
            waitForResults();
            then(results.get(results.size() - 1).latitude(), expected);
        }

        void thenCalculatedLongitudeIs(final double expected) throws Exception {
            waitForResults();
            then(results.get(results.size() - 1).longitude(), expected);
        }

        void thenCalculatedAltitudeIs(final double expected) throws Exception {
            waitForResults();
            then(results.get(results.size() - 1).altitude(), expected);
        }

        void thenSatelliteCountIs(final int expected) throws Exception {
            waitForResults();
            then(results.get(results.size() - 1).satelliteCount(), expected);
        }

        void thenCalculatedTimeIs(final java.time.LocalTime expected) throws Exception {
            waitForResults();
            then(results.get(results.size() - 1).utcTime(), expected);
        }

        private void waitForResults() throws Exception {
            Stream.generate(() -> {
                try { 
                    Thread.sleep(10); 
                } catch (InterruptedException e) { 
                    Thread.currentThread().interrupt(); 
                }
                return results.isEmpty();
            }).limit(200).takeWhile(empty -> empty).count();
            executor.shutdownNow();
        }
    }
}
