package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for TelemetryPulse.
 * Adheres to deterministic frozen clock verification (Phase 8) and Burst Materialization (Phase 9).
 */
class TelemetryPulseTest extends BddTest {

    private static final int MOCK_RTT = 10;
    private static final int MOCK_STRATUM = 1;
    private static final double MOCK_DISPERSION = 5.0;
    private static final double LAT_SAMPLE = 40.0;
    private static final double LON_SAMPLE = -80.0;
    private static final double ALT_SAMPLE = 100.0;
    private static final int SAT_SAMPLE = 8;
    private static final Instant MOCK_TIME = Instant.parse("2026-05-21T12:34:56.00Z");

    private final PulseFixture fixture = new ConnectorFixture();

    @Test
    void given_a_valid_burst_when_starting_pulse_then_id_is_generated() {
        fixture.given_burst(List.of("$GPRMC,123456,A*66", "$GPGGA,123456,..."));
        fixture.when_starting_pulse();
        fixture.then_id_is_not_empty();
        fixture.then_ingress_time_is_frozen();
    }

    @Test
    void given_a_pulse_when_updating_then_entire_burst_is_parsed() {
        fixture.given_burst(List.of("$GPRMC,123456,A*66", "$GPGGA,123456,..."));
        fixture.given_starting_pulse();
        fixture.given_mock_parser_returns_for_burst(new GpsData(LocalTime.now(), null, 0, 0, 0, 0));
        
        fixture.when_updating_pulse();
        
        fixture.then_fix_is_valid();
    }

    @Test
    void should_execute_logging_methods_without_exception() {
        fixture.given_burst(List.of("$GPRMC,123456,A*66", "$GPGGA,123456,..."));
        fixture.given_starting_pulse();
        fixture.given_mock_parser_returns_for_burst(new GpsData(LocalTime.now(), null, LAT_SAMPLE, LON_SAMPLE, ALT_SAMPLE, SAT_SAMPLE));
        fixture.when_updating_pulse();
        
        fixture.when_logging_raw();
        fixture.when_logging_final();
        
        fixture.then_logger_was_called();
        fixture.then_raw_logger_was_called();
    }

    private abstract static class PulseFixture {
        abstract void given_burst(List<String> burst);
        abstract void given_starting_pulse();
        abstract void given_pulse_without_ntp();
        abstract void given_mock_parser_returns_for_burst(GpsData data);
        abstract void when_starting_pulse();
        abstract void when_updating_pulse();
        abstract void when_logging_raw();
        abstract void when_logging_final();
        abstract void then_id_is_not_empty();
        abstract void then_ingress_time_is_frozen();
        abstract void then_fix_is_valid();
        abstract void then_logger_was_called();
        abstract void then_raw_logger_was_called();
    }

    private static final class ConnectorFixture extends PulseFixture {
        private List<String> burst;
        private TelemetryPulse pulse;
        private final NmeaParser mockParser = mock(NmeaParser.class);
        private final Logger mockLogger = mock(Logger.class);
        private final AtomicReference<GpsData> state = new AtomicReference<>(new GpsData(null, null, 0, 0, 0, 0));
        private final NtpResponse mockNtp = new NtpResponse(MOCK_TIME, MOCK_RTT, MOCK_STRATUM, MOCK_DISPERSION);

        @Override
        void given_burst(final List<String> b) {
            this.burst = b;
        }

        @Override
        void given_starting_pulse() {
            this.pulse = TelemetryPulse.start(
                burst, 
                mockNtp, 
                com.stoicprogrammer.qtrqth.model.ConfluenceHealth.HEALTHY_HARDWARE,
                MOCK_TIME
            );
        }

        @Override
        void given_pulse_without_ntp() {
            this.pulse = TelemetryPulse.start(
                burst, 
                null, 
                com.stoicprogrammer.qtrqth.model.ConfluenceHealth.HEALTHY_HARDWARE,
                MOCK_TIME
            );
        }

        @Override
        void given_mock_parser_returns_for_burst(final GpsData data) {
            given(mockParser.parseBurst(eq(burst), any())).willReturn(data);
        }

        @Override
        void when_starting_pulse() {
            this.pulse = TelemetryPulse.start(
                burst, 
                mockNtp, 
                com.stoicprogrammer.qtrqth.model.ConfluenceHealth.HEALTHY_HARDWARE,
                MOCK_TIME
            );
        }

        @Override
        void when_updating_pulse() {
            this.pulse = pulse.update(mockParser, state);
        }

        @Override
        void when_logging_raw() {
            pulse.logRaw(mockLogger);
        }

        @Override
        void when_logging_final() {
            pulse.logFinal(mockLogger);
        }

        @Override
        void then_id_is_not_empty() {
            assertThat(pulse.pulseId()).isNotNull().isNotEmpty();
        }

        @Override
        void then_ingress_time_is_frozen() {
            assertThat(pulse.ingressTime()).isEqualTo(MOCK_TIME);
        }

        @Override
        void then_fix_is_valid() {
            assertThat(pulse.hasValidFix()).isTrue();
        }

        @Override
        void then_logger_was_called() {
            verify(mockLogger, atLeastOnce()).info(anyString(), any(), any(), any(), any());
        }

        @Override
        void then_raw_logger_was_called() {
            verify(mockLogger, atLeastOnce()).debug(anyString(), anyString());
        }
    }
}
