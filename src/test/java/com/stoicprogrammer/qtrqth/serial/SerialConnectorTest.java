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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

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
    }

    @Test
    void givenAConnectedPort_whenDataArrives_thenNmeaSentencesAreInStream() throws Exception {
        fixture.givenPortExists("COM3");
        fixture.whenConnectingInAsyncThread("COM3");
        
        fixture.whenDataArrives("$GPRMC,123456,A*66\r\n");
        
        fixture.thenSentenceWasReceived("$GPRMC,123456,A*66");
    }

    private class ConnectorFixture {
        private final ConfigManager mockConfig = mock(ConfigManager.class);
        private final ISerialProvider mockProvider = mock(ISerialProvider.class);
        private final ISerialPort mockPort = mock(ISerialPort.class);
        private final NmeaSentenceAccumulator accumulator = new NmeaSentenceAccumulator();
        private final SerialConnector connector = new SerialConnector(mockConfig, accumulator, mockProvider);
        
        private final List<String> receivedSentences = new CopyOnWriteArrayList<>();
        private SerialPortDataListener capturedListener;
        private Stream<String> stream;
        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        void givenPortExists(final String name) {
            given(mockConfig.getProperty("serial.baud")).willReturn(Optional.of("9600"));
            given(mockProvider.getPort(name)).willReturn(mockPort);
            given(mockPort.openPort()).willReturn(true);
        }

        void whenConnecting(final String name) {
            this.stream = connector.connect(name);
            
            // Capture the listener
            final ArgumentCaptor<SerialPortDataListener> captor = ArgumentCaptor.forClass(SerialPortDataListener.class);
            verify(mockPort).addDataListener(captor.capture());
            capturedListener = captor.getValue();
        }

        void whenConnectingInAsyncThread(final String name) {
            whenConnecting(name);
            // Consume the stream in a background thread so we don't block
            executor.submit(() -> stream.forEach(receivedSentences::add));
        }

        void whenDataArrives(final String data) {
            final byte[] bytes = data.getBytes();
            given(mockPort.bytesAvailable()).willReturn(bytes.length);
            given(mockPort.readBytes(any(byte[].class), any(Integer.class))).willAnswer(invocation -> {
                final byte[] target = invocation.getArgument(0);
                System.arraycopy(bytes, 0, target, 0, bytes.length);
                return bytes.length;
            });

            // Trigger the event
            final SerialPortEvent event = mock(SerialPortEvent.class);
            given(event.getEventType()).willReturn(com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE);
            capturedListener.serialEvent(event);
        }

        void thenPortWasOpenedWithBaud(final int baud) {
            verify(mockPort).setBaudRate(baud);
            verify(mockPort).openPort();
        }

        void thenSentenceWasReceived(final String expected) throws Exception {
            // Functional wait using Stream recursion or limit
            Stream.generate(() -> {
                try { 
                    Thread.sleep(10); 
                } catch (InterruptedException e) { 
                    Thread.currentThread().interrupt(); 
                }
                return receivedSentences.contains(expected);
            }).limit(200).filter(found -> found).findFirst();

            thenTrue(receivedSentences.contains(expected));
            executor.shutdownNow();
        }
    }
}
