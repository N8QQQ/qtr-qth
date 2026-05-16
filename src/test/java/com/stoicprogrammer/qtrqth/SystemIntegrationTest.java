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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

/**
 * Business Rule: [SYSTEM INTEGRITY] - End-to-End Operational Flow.
 * Hardened in Phase 6.6 with Test Data Vaulting.
 */
class SystemIntegrationTest extends BddTest {

    private final SystemFixture fixture = new SystemFixture();

    @Test
    void should_process_vaulted_telemetry_through_full_pipeline() throws Exception {
        fixture.given_simulated_hardware_ready();
        
        // Stream every sentence from the shack sample into the virtual hardware
        getTelemetrySentences("shack_sample_01.nmea").forEach(fixture::when_serial_data_arrives);
        
        fixture.then_calculated_latitude_is(46.28342983333333);
        fixture.then_calculated_longitude_is(-87.88802466666667);
        fixture.then_calculated_altitude_is(425.1);
        fixture.then_satellite_count_is(8);
        fixture.then_calculated_time_is(java.time.LocalTime.of(23, 28, 10));
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

        void given_simulated_hardware_ready() {
            given(mockConfig.getProperty("serial.baud")).willReturn(Optional.of("9600"));
            given(mockProvider.getPort("SIM1")).willReturn(mockPort);
            given(mockPort.openPort()).willReturn(true);
            
            final AtomicReference<GpsData> state = new AtomicReference<>(new GpsData(null, null, 0, 0, 0, 0));
            
            final Stream<String> stream = connector.connect("SIM1");
            executor.submit(() -> stream.map(s -> state.updateAndGet(fix -> parser.parse(s, fix)))
                                        .forEach(results::add));

            final ArgumentCaptor<com.fazecast.jSerialComm.SerialPortDataListener> captor = ArgumentCaptor.forClass(com.fazecast.jSerialComm.SerialPortDataListener.class);
            then(mockPort).should().addDataListener(captor.capture());
            capturedListener = captor.getValue();
        }

        void when_serial_data_arrives(final String raw) {
            // Append CRLF as hardware would
            final byte[] bytes = (raw.trim() + "\r\n").getBytes();
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

        void then_calculated_latitude_is(final double expected) throws Exception {
            waitForResults();
            assertThat(results.get(results.size() - 1).latitude()).isCloseTo(expected, org.assertj.core.data.Offset.offset(0.000001));
        }

        void then_calculated_longitude_is(final double expected) throws Exception {
            waitForResults();
            assertThat(results.get(results.size() - 1).longitude()).isCloseTo(expected, org.assertj.core.data.Offset.offset(0.000001));
        }

        void then_calculated_altitude_is(final double expected) throws Exception {
            waitForResults();
            assertThat(results.get(results.size() - 1).altitude()).isCloseTo(expected, org.assertj.core.data.Offset.offset(0.000001));
        }

        void then_satellite_count_is(final int expected) throws Exception {
            waitForResults();
            assertThat(results.get(results.size() - 1).satelliteCount()).isEqualTo(expected);
        }

        void then_calculated_time_is(final java.time.LocalTime expected) throws Exception {
            waitForResults();
            assertThat(results.get(results.size() - 1).utcTime()).isEqualTo(expected);
        }

        private void waitForResults() {
            // Functional Polling: Use a stream to wait for results without an imperative loop
            Stream.generate(() -> {
                try { Thread.sleep(10); } 
                catch (final InterruptedException e) { Thread.currentThread().interrupt(); }
                return results.isEmpty();
            }).limit(200).takeWhile(empty -> empty).count();
        }
    }
}
