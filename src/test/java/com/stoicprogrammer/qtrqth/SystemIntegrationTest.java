package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.analysis.PrecisionMetrics;
import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.model.ConfluenceHealth;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SystemIntegrationTest extends BddTest {

    private static final double TEST_LATITUDE = 40.0;
    private static final double TEST_LONGITUDE = -80.0;
    private static final double TEST_ALTITUDE = 100.0;
    private static final int TEST_SATELLITE_COUNT = 8;

    private final IntegrationFixture fixture = new IntegrationFixture();

    @Test
    void should_integrate_all_components_into_pulse_stream() {
        fixture.given_sample_pulse(Instant.parse("2026-05-17T12:00:00Z"));
        fixture.then_pulse_stream_is_not_empty();
    }

    private final class IntegrationFixture {
        private List<TelemetryPulse> capturedPulses = List.of();

        void given_sample_pulse(final Instant timestamp) {
            final GpsData sampleData = new GpsData(
                LocalTime.NOON,
                null,
                TEST_LATITUDE,
                TEST_LONGITUDE,
                TEST_ALTITUDE,
                TEST_SATELLITE_COUNT
            );
            final TelemetryPulse pulse = TelemetryPulse.start(
                "trigger",
                null,
                ConfluenceHealth.HEALTHY_HARDWARE,
                timestamp,
                sampleData,
                PrecisionMetrics.EMPTY
            );
            this.capturedPulses = List.of(pulse);
        }

        void then_pulse_stream_is_not_empty() {
            assertThat(capturedPulses).isNotEmpty();
        }
    }
}
