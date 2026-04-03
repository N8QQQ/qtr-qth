package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator;
import com.stoicprogrammer.qtrqth.serial.SerialConnector;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Business Rule: [SYSTEM INTEGRITY] - End-to-End Operational Flow.
 * Verify that data flows from the Serial Provider through the Nibbler to the Parser correctly.
 */
class SystemIntegrationTest extends BddTest {

    private final SystemFixture fixture = new SystemFixture();

    @Test
    void givenSimulatedHardware_whenDataFlows_thenFinalGpsDataIsProduced() {
        fixture.givenSimulatedHardwareReady();
        fixture.whenSerialDataArrives("$GPRMC,123456,A,4000.000,N,08000.000,W,0,0,010126,,,A\r\n");
        fixture.thenCalculatedLatitudeIs(40.0);
        fixture.thenCalculatedLongitudeIs(-80.0);
    }

    private class SystemFixture {
        private final ISerialProvider mockProvider = mock(ISerialProvider.class);
        private final ISerialPort mockPort = mock(ISerialPort.class);
        private final ConfigManager mockConfig = mock(ConfigManager.class);
        private final NmeaSentenceAccumulator accumulator = new NmeaSentenceAccumulator();
        private final NmeaParser parser = new NmeaParser();
        private final SerialConnector connector = new SerialConnector(mockConfig, accumulator, mockProvider);
        
        private com.fazecast.jSerialComm.SerialPortDataListener capturedListener;
        private final List<com.stoicprogrammer.qtrqth.nmea.GpsData> results = new ArrayList<>();

        void givenSimulatedHardwareReady() {
            given(mockConfig.getProperty("serial.baud")).willReturn("9600");
            given(mockProvider.getPort("SIM1")).willReturn(mockPort);
            given(mockPort.openPort()).willReturn(true);
            
            connector.connect("SIM1", sentence -> {
                results.add(parser.parse(sentence));
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

        void thenCalculatedLatitudeIs(double expected) {
            then(results.get(0).getLatitude(), expected);
        }

        void thenCalculatedLongitudeIs(double expected) {
            then(results.get(0).getLongitude(), expected);
        }
    }
}
