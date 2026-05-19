package com.stoicprogrammer.qtrqth.model;

import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import com.stoicprogrammer.qtrqth.util.GridSquareCalculator;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The 'Pulse' is the fundamental domain model of the qtr-qth system.
 * It represents the high-fidelity confluence of GPS telemetry and NTP reference.
 */
public record TelemetryPulse(
    String pulseId, 
    String sentence, 
    GpsData data, 
    NtpResponse reference,
    ConfluenceHealth health
) {
    // Mask for 16-bit Pulse ID generation
    private static final int PULSE_ID_MASK = 0xFFFF;

    /**
     * Factory method to initiate a new telemetry heartbeat.
     * @param sentence The raw NMEA sentence.
     * @param ntp The latest known NTP reference.
     * @param health The current system health status.
     * @return A new TelemetryPulse instance.
     */
    public static TelemetryPulse start(final String sentence, final NtpResponse ntp, final ConfluenceHealth health) {
        final String id = String.format("%04X", (sentence.hashCode() & PULSE_ID_MASK));
        return new TelemetryPulse(id, sentence, Optional.empty(), Optional.ofNullable(ntp), health);
    }

    /**
     * Constructor for internal evolution of the pulse.
     */
    private TelemetryPulse(
        final String pulseId, 
        final String sentence, 
        final Optional<GpsData> data, 
        final Optional<NtpResponse> reference,
        final ConfluenceHealth health
    ) {
        this(pulseId, sentence, data.orElse(null), reference.orElse(null), health);
    }

    /**
     * Logs the raw telemetry data within the pulse's diagnostic context.
     */
    public void logRaw(final Logger log) {
        runWithContext(() -> log.debug("[RAW] {}", sentence));
    }

    /**
     * Evolves the pulse state by parsing the NMEA sentence and merging it with system fix state.
     */
    public TelemetryPulse update(final NmeaParser parser, final AtomicReference<GpsData> state) {
        final GpsData next = state.updateAndGet(fix -> parser.parse(sentence, fix));
        return new TelemetryPulse(pulseId, sentence, Optional.of(next), Optional.ofNullable(reference), health);
    }

    /**
     * Verifies if the pulse carries a valid GPS fix.
     */
    public boolean hasValidFix() {
        return Optional.ofNullable(data)
            .flatMap(d -> Optional.ofNullable(d.utcTime()))
            .isPresent();
    }

    /**
     * Logs the final high-fidelity telemetry result within the pulse's diagnostic context.
     */
    public void logFinal(final Logger log) {
        runWithContext(() -> {
            final String grid = GridSquareCalculator.calculate(data.latitude(), data.longitude());
            final String ntpStatus = Optional.ofNullable(reference)
                .map(r -> String.format("NTP: %s (RTT: %dms, Stratum: %d)", r.time(), r.rttMs(), r.stratum()))
                .orElse("NTP: No Reference");
            
            final String healthMsg = String.format("[GPS: %s | NTP: %s | Mode: %s]", 
                health.gpsStatus(), health.ntpStatus(), health.mode());
            
            log.info("{} GPS Fix: {} | {} | Grid: {}", healthMsg, data, ntpStatus, grid);
        });
    }

    /**
     * Helper to execute operations within the Mapped Diagnostic Context (MDC).
     */
    private void runWithContext(final Runnable action) {
        MDC.put("pulseId", pulseId);
        try {
            action.run();
        } finally {
            MDC.clear();
        }
    }
}
