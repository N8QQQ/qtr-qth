package com.stoicprogrammer.qtrqth.serial;

import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Business Rule: [PHASE 2, STEP 2 & 3] - Serial Connection & Data Ingestion.
 */
class SerialConnectorTest extends BddTest {

    private final ConnectorFixture fixture = new ConnectorFixture();

    @Test
    void givenAValidPort_whenConnecting_thenPortIsConfiguredAndOpened() {
        fixture.givenPortExists("COM3");
        fixture.whenConnecting("COM3");
        fixture.thenPortWasOpenedWithBaud(9600);
        fixture.thenListeningEventsAreCorrect();
    }

    @Test
    void givenAPortThatFailsToOpen_whenConnecting_thenReturnsFalse() {
        fixture.givenPortFailsToOpen("COM3");
        fixture.whenConnecting("COM3");
        fixture.thenConnectionFailed();
    }

    @Test
    void givenAConnectedPort_whenDataArrives_thenNmeaSentencesArePassedToHandler() {
        fixture.givenPortExists("COM3");
        fixture.whenConnecting("COM3");
        
        fixture.whenDataArrives("$GPRMC,123456,A*66\r\n");
        
        fixture.thenSentenceWasReceived("$GPRMC,123456,A*66");
    }

    @Test
    void givenAConnectedPort_whenNonDataEventOccurs_thenDataIsNotProcessed() {
        fixture.givenPortExists("COM3");
        fixture.whenConnecting("COM3");
        
        fixture.whenNonDataEventOccurs();
        
        fixture.thenNoDataWasRead();
    }

    @Test
    void givenAnOpenPort_whenDisconnecting_thenPortIsClosed() {
        fixture.givenPortExists("COM3");
        fixture.whenConnecting("COM3");
        fixture.givenPortIsOpen();
        
        fixture.whenDisconnecting();
        
        fixture.thenPortWasClosed();
    }

    private class ConnectorFixture {
        private final ConfigManager mockConfig = mock(ConfigManager.class);
        private final ISerialProvider mockProvider = mock(ISerialProvider.class);
        private final ISerialPort mockPort = mock(ISerialPort.class);
        private final NmeaSentenceAccumulator accumulator = new NmeaSentenceAccumulator();
        private final SerialConnector connector = new SerialConnector(mockConfig, accumulator, mockProvider);
        
        private final List<String> receivedSentences = new ArrayList<>();
        private SerialPortDataListener capturedListener;
        private boolean connectResult;

        void givenPortExists(String name) {
            givenStubbing(mockConfig.getProperty("serial.baud")).willReturn("9600");
            givenStubbing(mockProvider.getPort(name)).willReturn(mockPort);
            givenStubbing(mockPort.openPort()).willReturn(true);
        }

        void givenPortIsOpen() {
            givenStubbing(mockPort.isOpen()).willReturn(true);
        }

        void givenPortFailsToOpen(String name) {
            givenStubbing(mockConfig.getProperty("serial.baud")).willReturn("9600");
            givenStubbing(mockProvider.getPort(name)).willReturn(mockPort);
            givenStubbing(mockPort.openPort()).willReturn(false);
        }

        void whenConnecting(String name) {
            connectResult = connector.connect(name, receivedSentences::add);
            
            // Capture the listener only if it was added
            if (connectResult) {
                ArgumentCaptor<SerialPortDataListener> captor = ArgumentCaptor.forClass(SerialPortDataListener.class);
                verify(mockPort).addDataListener(captor.capture());
                capturedListener = captor.getValue();
            }
        }

        void whenDataArrives(String data) {
            byte[] bytes = data.getBytes();
            givenStubbing(mockPort.bytesAvailable()).willReturn(bytes.length);
            givenStubbing(mockPort.readBytes(any(byte[].class), any(Integer.class))).willAnswer(invocation -> {
                byte[] target = invocation.getArgument(0);
                System.arraycopy(bytes, 0, target, 0, bytes.length);
                return bytes.length;
            });

            // Trigger the event
            SerialPortEvent event = mock(SerialPortEvent.class);
            givenStubbing(event.getEventType()).willReturn(com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE);
            capturedListener.serialEvent(event);
        }

        void whenNonDataEventOccurs() {
            SerialPortEvent event = mock(SerialPortEvent.class);
            givenStubbing(event.getEventType()).willReturn(0); // Not DATA_AVAILABLE
            capturedListener.serialEvent(event);
        }

        void whenDisconnecting() {
            connector.disconnect();
        }

        void thenPortWasOpenedWithBaud(int baud) {
            verify(mockPort).setBaudRate(baud);
            verify(mockPort).openPort();
            thenTrue(connectResult);
        }

        void thenConnectionFailed() {
            thenTrue(!connectResult);
        }

        void thenListeningEventsAreCorrect() {
            then(capturedListener.getListeningEvents(), com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE);
        }

        void thenNoDataWasRead() {
            verify(mockPort, org.mockito.Mockito.never()).readBytes(any(), any(Integer.class));
        }

        void thenPortWasClosed() {
            verify(mockPort).closePort();
        }

        void thenSentenceWasReceived(String expected) {
            thenTrue(receivedSentences.contains(expected));
        }
    }
}
