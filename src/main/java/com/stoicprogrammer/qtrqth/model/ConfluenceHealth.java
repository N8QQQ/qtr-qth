package com.stoicprogrammer.qtrqth.model;

/**
 * High-fidelity health status for the telemetry rivers.
 * Enables deterministic signaling of signal loss and recovery states.
 */
public record ConfluenceHealth(
    RiverStatus gpsStatus,
    RiverStatus ntpStatus,
    OperationalMode mode,
    SyncStatus syncStatus
) {
    /**
     * Synchronization states for the telemetry confluence.
     */
    public enum SyncStatus {
        UNKNOWN,       // Initial state
        REACTIVE_LOCK, // Zero-latency event trigger achieved
        SIGNAL_LOSS,   // Telemetry stream interrupted
        SIMULATION     // Operating on synthetic data
    }

    /**
     * Standard status levels for individual data rivers.
     */
    public enum RiverStatus {
        ACTIVE,    // Stream is flowing with valid data
        RECOVERY,  // Stream is interrupted; system is monitoring for restoration
        OFFLINE    // Stream is explicitly disabled or uninitialized
    }

    /**
     * The locked operational persona of the confluence.
     */
    public enum OperationalMode {
        HARDWARE_LOCK,   // Committed to physical hardware (with fail-safe recovery)
        SIMULATION_LOCK  // Committed to virtualized/simulated streams
    }

    /**
     * Standard healthy baseline.
     */
    public static final ConfluenceHealth HEALTHY_HARDWARE = new ConfluenceHealth(
        RiverStatus.ACTIVE, 
        RiverStatus.ACTIVE, 
        OperationalMode.HARDWARE_LOCK,
        SyncStatus.REACTIVE_LOCK
    );

    /**
     * Standard healthy simulation baseline.
     */
    public static final ConfluenceHealth HEALTHY_SIMULATION = new ConfluenceHealth(
        RiverStatus.ACTIVE, 
        RiverStatus.ACTIVE, 
        OperationalMode.SIMULATION_LOCK,
        SyncStatus.SIMULATION
    );
}
