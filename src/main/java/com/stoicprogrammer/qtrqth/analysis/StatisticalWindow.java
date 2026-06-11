package com.stoicprogrammer.qtrqth.analysis;

import io.vavr.collection.Vector;
import io.vavr.control.Option;
import java.time.Duration;

/**
 * Stateful functional analyzer for statistical clock jitter.
 * Employs an O(1) incremental update model for sliding window statistics.
 */
public record StatisticalWindow(
    Vector<Duration> offsets,
    int maxSize,
    double sumMicroseconds,
    double sumSqMicroseconds
) {

    private static final double NANOS_TO_MICROS = 1000.0;

    public static StatisticalWindow empty(final int maxSize) {
        return new StatisticalWindow(Vector.empty(), maxSize, 0.0, 0.0);
    }

    /**
     * Appends a new offset to the window and updates running statistics.
     * Logic is branchless to maintain functional purity.
     */
    public StatisticalWindow add(final Duration offset) {
        final double valMicroseconds = offset.toNanos() / NANOS_TO_MICROS;
        final boolean isFull = offsets.size() >= maxSize;

        final double oldMicroseconds = isFull ? offsets.head().toNanos() / NANOS_TO_MICROS : 0.0;
        
        final Vector<Duration> nextOffsets = isFull 
            ? offsets.tail().append(offset) 
            : offsets.append(offset);

        return new StatisticalWindow(
            nextOffsets,
            maxSize,
            sumMicroseconds + valMicroseconds - oldMicroseconds,
            sumSqMicroseconds + (valMicroseconds * valMicroseconds) - (oldMicroseconds * oldMicroseconds)
        );
    }

    /**
     * Calculates the Root Mean Square (RMS) of the window in O(1).
     */
    public double rmsJitterMicroseconds() {
        return Option.of(offsets.size())
            .filter(n -> n > 0)
            .map(n -> Math.sqrt(sumSqMicroseconds / n))
            .getOrElse(0.0);
    }

    /**
     * Calculates the Standard Deviation (Stability) of the window in O(1).
     */
    public double stabilityMicroseconds() {
        return Option.of(offsets.size())
            .filter(n -> n > 0)
            .map(n -> {
                final double mean = sumMicroseconds / n;
                final double variance = (sumSqMicroseconds / n) - (mean * mean);
                // Math.max to handle potential floating point precision noise underflowing to -0.0
                return Math.sqrt(Math.max(0.0, variance));
            })
            .getOrElse(0.0);
    }
}
