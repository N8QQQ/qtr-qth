package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for TelemetryPulse.
 * Adheres to deterministic frozen clock verification (Phase 8) and Reactive Triggers (Phase 9).
 */
class TelemetryPulseTest extends BddTest {

    private static final int MOCK_RTT = 10;
    private static final int MOCK_STRATUM = 1;
    private static final double MOCK_DISPERSION = 5.0;
    private static final Instant MOCK_TIME = Instant.parse("2026-05-21T12:34:56.00Z");

    private final PulseFixture fixture = new ConnectorFixture();

    @Test
    void given_a_valid_trigger_when_starting_pulse_then_id_is_generated() {
        fixture.given_trigger("$GPZDA,123456.00,21,05,2026,00,00*6D");
        fixture.when_starting_pulse();
        fixture.then_id_is_not_empty();
        fixture.then_ingress_time_is_frozen();
    }

    @Test
    void should_execute_logging_methods_without_exception() {
        fixture.given_trigger("$GPZDA,123456.00,21,05,2026,00,00*6D");
        fixture.given_starting_pulse();
        
        fixture.when_logging_final();
        
        fixture.then_logger_was_called();
    }

    private abstract static class PulseFixture {
        abstract void given_trigger(String sentence);
        abstract void given_starting_pulse();
        abstract void when_starting_pulse();
        abstract void when_logging_final();
        abstract void then_id_is_not_empty();
        abstract void then_ingress_time_is_frozen();
        abstract void then_logger_was_called();
    }

    private static final class ConnectorFixture extends PulseFixture {
        private String trigger;
        private TelemetryPulse pulse;
        private final Logger mockLogger = mock(Logger.class);
        private final NtpResponse mockNtp = new NtpResponse(MOCK_TIME, MOCK_RTT, MOCK_STRATUM, MOCK_DISPERSION);
        private final GpsData sampleData = new GpsData(LocalTime.of(12, 34, 56), null, 40.0, -80.0, 100.0, 8);

        @Override
        void given_trigger(final String s) {
            this.trigger = s;
        }

        @Override
        void given_starting_pulse() {
            this.pulse = TelemetryPulse.start(
                trigger, 
                mockNtp, 
                com.stoicprogrammer.qtrqth.model.ConfluenceHealth.HEALTHY_HARDWARE,
                MOCK_TIME,
                sampleData
            );
        }

        @Override
        void when_starting_pulse() {
            this.pulse = TelemetryPulse.start(
                trigger, 
                mockNtp, 
                com.stoicprogrammer.qtrqth.model.ConfluenceHealth.HEALTHY_HARDWARE,
                MOCK_TIME,
                sampleData
            );
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
        void then_logger_was_called() {
            verify(mockLogger, atLeastOnce()).info(anyString(), any(), any(), any(), any());
        }
    }
}
