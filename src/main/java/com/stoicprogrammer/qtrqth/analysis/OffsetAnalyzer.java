package com.stoicprogrammer.qtrqth.analysis;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Optional;

public final class OffsetAnalyzer {

    private OffsetAnalyzer() {
        // Utility class
    }
    
    public static Duration calculateOffset(final Instant systemTime, final LocalTime gpsTime) {
        return Optional.ofNullable(gpsTime)
            .map(t -> t.atDate(systemTime.atZone(ZoneOffset.UTC).toLocalDate()).toInstant(ZoneOffset.UTC))
            .map(gpsInstant -> Duration.between(gpsInstant, systemTime))
            .orElse(Duration.ZERO);
    }
}
