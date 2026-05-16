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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Business Rule: [PHASE 2, STEP 2 & 3] - Serial Connection & Data Ingestion.
 */
class SerialConnectorTest extends BddTest {

    private final ConnectorFixture fixture = new ConnectorFixture();

    @Test
    void given_a_valid_port_when_connecting_then_port_is_configured_and_opened() {
        fixture.given_port_exists("COM3");
        fixture.when_connecting("COM3");
        fixture.then_port_was_opened_with_baud(9600);
    }

    @Test
    void given_a_connected_port_when_data_arrives_then_nmea_sentences_are_in_stream() throws Exception {
        fixture.given_port_exists("COM3");
        fixture.when_connecting_in_async_thread("COM3");
        
        fixture.when_data_arrives("$GPRMC,123456,A*66\r\n");
        
        fixture.then_sentence_was_received("$GPRMC,123456,A*66");
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

        void given_port_exists(final String name) {
            given(mockConfig.getProperty("serial.baud")).willReturn(Optional.of("9600"));
            given(mockProvider.getPort(name)).willReturn(mockPort);
            given(mockPort.openPort()).willReturn(true);
            given(mockPort.isOpen()).willReturn(true);
        }

        void when_connecting(final String name) {
            this.stream = connector.connect(name);
            
            // Capture the listener
            final ArgumentCaptor<SerialPortDataListener> captor = ArgumentCaptor.forClass(SerialPortDataListener.class);
            then(mockPort).should().addDataListener(captor.capture());
            capturedListener = captor.getValue();
        }

        void when_connecting_in_async_thread(final String name) {
            when_connecting(name);
            executor.submit(() -> stream.forEach(receivedSentences::add));
        }

        void when_data_arrives(final String data) {
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

        void then_port_was_opened_with_baud(final int baud) {
            then(mockPort).should().setBaudRate(baud);
            then(mockPort).should().openPort();
        }

        void then_sentence_was_received(final String expected) throws Exception {
            Stream.generate(() -> {
                try { 
                    Thread.sleep(10); 
                } catch (final InterruptedException e) { 
                    Thread.currentThread().interrupt(); 
                }
                return receivedSentences.contains(expected);
            }).limit(200).filter(found -> found).findFirst();

            assertThat(receivedSentences).contains(expected);
            executor.shutdownNow();
        }
    }
}
