package com.stoicprogrammer.qtrqth.config;

import java.util.List;

/**
 * Immutable configuration record for the qtr-qth hub.
 * Centralizes all typed system parameters to eliminate Stringly-Typed logic.
 */
public record AppConfig(
    List<String> ntpPool,
    int serialBaud,
    long syncThresholdMs,
    List<String> discoveryKeywords,
    boolean displayRawTelemetry,
    boolean simulationMode,
    SyncPolicy syncPolicy,
    int syncCalibrationCycles,
    int syncCalibrationTimeoutSeconds
) {
    /**
     * Operational policies for hardware synchronization.
     */
    public enum SyncPolicy {
        STRICT,   // Fail-stop if calibration fails
        FLEXIBLE  // Fallback to temporal bucketing if calibration fails
    }

    /**
     * Default configuration for rapid bootstrapping.
     */
    public static final AppConfig DEFAULT = new AppConfig(
        List.of("pool.ntp.org", "time.google.com", "time.windows.com"),
        9600,
        1000L,
        List.of("gps", "u-blox", "prolific", "silicon labs", "gnss", "receiver", "ttyusb"),
        false,
        false,
        SyncPolicy.FLEXIBLE,
        3,
        30
    );
}

