package com.stoicprogrammer.qtrqth.analysis;

import java.time.Duration;

public record PrecisionMetrics(
    Duration systemOffset,
    double rmsJitterMicroseconds,
    double stabilityMicroseconds
) {
    public static final PrecisionMetrics EMPTY = new PrecisionMetrics(Duration.ZERO, 0.0, 0.0);
}
