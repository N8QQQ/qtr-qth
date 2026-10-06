package com.stoicprogrammer.qtrqth.analysis;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import static org.assertj.core.api.Assertions.assertThat;

class OffsetAnalyzerTest extends BddTest {

    private static final long POSITIVE_OFFSET_MS = 500L;
    private static final long NEGATIVE_OFFSET_MS = -500L;

    private final AnalyzerFixture fixture = new AnalyzerFixture();

    @Test
    void should_calculate_positive_offset_when_system_is_ahead() {
        fixture.given_system_time(Instant.parse("2026-05-27T12:00:00.500Z"));
        fixture.given_gps_time(LocalTime.NOON);
        fixture.when_calculating_offset();
        fixture.then_offset_millis_is(POSITIVE_OFFSET_MS);
    }

    @Test
    void should_calculate_negative_offset_when_system_is_behind() {
        fixture.given_system_time(Instant.parse("2026-05-27T11:59:59.500Z"));
        fixture.given_gps_time(LocalTime.NOON);
        fixture.when_calculating_offset();
        fixture.then_offset_millis_is(NEGATIVE_OFFSET_MS);
    }

    private final class AnalyzerFixture {
        private Instant systemTime;
        private LocalTime gpsTime;
        private Duration calculatedOffset;

        void given_system_time(final Instant time) {
            this.systemTime = time;
        }

        void given_gps_time(final LocalTime time) {
            this.gpsTime = time;
        }

        void when_calculating_offset() {
            this.calculatedOffset = OffsetAnalyzer.calculateOffset(systemTime, gpsTime);
        }

        void then_offset_millis_is(final long expectedMillis) {
            assertThat(calculatedOffset.toMillis()).isEqualTo(expectedMillis);
        }
    }
}
