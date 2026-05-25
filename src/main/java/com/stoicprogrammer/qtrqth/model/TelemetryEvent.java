package com.stoicprogrammer.qtrqth.model;

import java.time.Instant;

/**
 * Immutable event container for high-fidelity telemetry hand-off.
 * Bundles the raw NMEA sentence with its producer-side Edge Stamp (T1).
 */
public record TelemetryEvent(
    String rawSentence,
    Instant ingressTime
) {
    /**
     * Sentinel for signaling stream termination.
     */
    public static final TelemetryEvent SIGNAL_LOSS = new TelemetryEvent("__SIGNAL_LOSS__", Instant.EPOCH);

    /**
     * Identifies if this event represents a legitimate signal loss.
     */
    public boolean isSignalLoss() {
        return rawSentence.equals(SIGNAL_LOSS.rawSentence());
    }
}
