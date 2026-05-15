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
 */
class SystemIntegrationTest extends BddTest {

    private final SystemFixture fixture = new SystemFixture();

    @Test
    void given_simulated_hardware_when_data_flows_then_final_gps_data_is_produced() throws Exception {
        fixture.given_simulated_hardware_ready();
        
        fixture.when_serial_data_arrives("$GPRMC,123456,A,4000.000,N,08000.000,W,0,0,010126,,,A\r\n");
        fixture.when_serial_data_arrives("$GPGGA,123456,4000.000,N,08000.000,W,1,08,1.0,100.0,M,,M,,\r\n");
        
        fixture.then_calculated_latitude_is(40.0);
        fixture.then_calculated_longitude_is(-80.0);
        fixture.then_calculated_altitude_is(100.0);
        fixture.then_satellite_count_is(8);
        fixture.then_calculated_time_is(java.time.LocalTime.of(12, 34, 56));
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

        private void waitForResults() throws Exception {
            Stream.generate(() -> {
                try { 
                    Thread.sleep(10); 
                } catch (final InterruptedException e) { 
                    Thread.currentThread().interrupt(); 
                }
                return results.isEmpty();
            }).limit(200).takeWhile(empty -> empty).count();
            executor.shutdownNow();
        }
    }
}
