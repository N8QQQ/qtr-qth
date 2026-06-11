package com.stoicprogrammer.qtrqth.analysis;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import static org.assertj.core.api.Assertions.assertThat;

class OffsetAnalyzerTest {

    private static final long POSITIVE_OFFSET_MS = 500L;
    private static final long NEGATIVE_OFFSET_MS = -500L;

    @Test
    void should_calculate_positive_offset_when_system_is_ahead() {
        final Instant systemTime = Instant.parse("2026-05-27T12:00:00.500Z");
        final LocalTime gpsTime = LocalTime.of(12, 0, 0);
        
        final Duration offset = OffsetAnalyzer.calculateOffset(systemTime, gpsTime);
        
        assertThat(offset.toMillis()).isEqualTo(POSITIVE_OFFSET_MS);
    }

    @Test
    void should_calculate_negative_offset_when_system_is_behind() {
        final Instant systemTime = Instant.parse("2026-05-27T11:59:59.500Z");
        final LocalTime gpsTime = LocalTime.of(12, 0, 0);
        
        final Duration offset = OffsetAnalyzer.calculateOffset(systemTime, gpsTime);
        
        assertThat(offset.toMillis()).isEqualTo(NEGATIVE_OFFSET_MS);
    }
}
