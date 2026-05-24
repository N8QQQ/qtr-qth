package com.stoicprogrammer.qtrqth.serial;

import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.model.TelemetryEvent;
import com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.time.InstantSource;
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

class SerialConnectorTest extends BddTest {

    private static final int EXPECTED_BAUD = 9600;
    private static final Instant MOCK_TIME = Instant.parse("2026-05-24T12:00:00Z");
    private final ConnectorFixture fixture = new ConnectorFixture();

    @Test
    void should_connect_to_specified_port_with_correct_baud() {
        fixture.given_port_exists("COM3");
        fixture.when_connecting("COM3");
        fixture.then_port_was_opened_with_baud(EXPECTED_BAUD);
    }

    @Test
    void should_ingest_data_from_serial_event_stream_with_edge_stamp() {
        final String expected = "$GPRMC,1,2,3*44";
        fixture.given_port_exists("COM4");
        fixture.when_connecting_in_async_thread("COM4");
        fixture.when_data_arrives(expected);
        fixture.then_event_is_received_with_stamp(expected, MOCK_TIME);
    }

    @Test
    void should_return_empty_stream_when_port_fails_to_open() {
        fixture.given_port_cannot_be_opened("COM_FAIL");
        fixture.when_connecting_without_capture("COM_FAIL");
        fixture.then_stream_is_empty();
    }

    private final class ConnectorFixture {
        private static final int DEFAULT_BAUD = 9600;
        private static final int SLEEP_INTERVAL_MS = 10;
        private static final int MAX_WAIT_ATTEMPTS = 200;

        private final ISerialProvider mockProvider = mock(ISerialProvider.class);
        private final ISerialPort mockPort = mock(ISerialPort.class);
        private final ConfigManager mockConfig = mock(ConfigManager.class);
        private final NmeaSentenceAccumulator accumulator = new NmeaSentenceAccumulator();
        private final InstantSource mockClock = InstantSource.fixed(MOCK_TIME);
        private final SerialConnector connector = new SerialConnector(mockConfig, accumulator, mockProvider, mockClock);
        
        private Stream<TelemetryEvent> stream;
        private com.fazecast.jSerialComm.SerialPortDataListener capturedListener;
        private final List<TelemetryEvent> receivedEvents = new CopyOnWriteArrayList<>();
        private final ExecutorService executor = Executors.newSingleThreadExecutor();

        void given_port_exists(final String name) {
            given(mockConfig.getProperty("serial.baud")).willReturn(Optional.of(String.valueOf(DEFAULT_BAUD)));
            given(mockProvider.getPort(name)).willReturn(mockPort);
            given(mockPort.openPort()).willReturn(true);
            given(mockPort.isOpen()).willReturn(true);
        }

        void given_port_cannot_be_opened(final String name) {
            given(mockProvider.getPort(name)).willReturn(mockPort);
            given(mockPort.openPort()).willReturn(false);
        }

        void when_connecting(final String name) {
            this.stream = connector.connect(name);
            
            // Capture the listener
            final ArgumentCaptor<SerialPortDataListener> captor = ArgumentCaptor.forClass(SerialPortDataListener.class);
            then(mockPort).should().addDataListener(captor.capture());
            capturedListener = captor.getValue();
        }

        void when_connecting_without_capture(final String name) {
            this.stream = connector.connect(name);
        }

        void when_connecting_in_async_thread(final String name) {
            when_connecting(name);
            executor.submit(() -> stream.forEach(receivedEvents::add));
        }

        void when_data_arrives(final String data) {
            final byte[] bytes = (data.trim() + "\r\n").getBytes();
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

        void then_stream_is_empty() {
            assertThat(stream).isEmpty();
        }

        void then_event_is_received_with_stamp(final String expected, final Instant stamp) {
            Stream.generate(() -> {
               try { 
                   Thread.sleep(SLEEP_INTERVAL_MS); 
               } catch (final InterruptedException e) { 
                   Thread.currentThread().interrupt(); 
               }
               return receivedEvents.stream().anyMatch(e -> e.rawSentence().equals(expected));
            }).limit(MAX_WAIT_ATTEMPTS).filter(found -> found).findFirst();

            assertThat(receivedEvents).anyMatch(e -> e.rawSentence().equals(expected) && e.ingressTime().equals(stamp));
            executor.shutdownNow();
        }
    }
}
