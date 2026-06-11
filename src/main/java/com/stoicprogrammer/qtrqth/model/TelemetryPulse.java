package com.stoicprogrammer.qtrqth.model;

import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.util.GridSquareCalculator;
import com.stoicprogrammer.qtrqth.analysis.PrecisionMetrics;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.time.Instant;
import java.util.Optional;

public record TelemetryPulse(
    String pulseId, 
    String triggeringSentence, 
    Instant ingressTime, 
    GpsData data, 
    NtpResponse reference, 
    ConfluenceHealth health, 
    PrecisionMetrics precision
) {
    private static final int PULSE_ID_MASK = 0xFFFF;

    public static TelemetryPulse start(
        final String trigger, 
        final NtpResponse ntp, 
        final ConfluenceHealth health, 
        final Instant ingressTime, 
        final GpsData enrichedData, 
        final PrecisionMetrics precision
    ) {
        final String id = String.format("%04X", (trigger.hashCode() & PULSE_ID_MASK));
        return new TelemetryPulse(id, trigger, ingressTime, enrichedData, ntp, health, precision);
    }

    public boolean hasValidFix() {
        return Optional.ofNullable(data)
            .flatMap(d -> Optional.ofNullable(d.utcTime()))
            .isPresent();
    }

    public boolean isHeartbeat() {
        return triggeringSentence.equals(TelemetryEvent.SIGNAL_LOSS.rawSentence());
    }

    public void logFinal(final Logger log) {
        runWithContext(() -> {
            final String grid = GridSquareCalculator.calculate(data.latitude(), data.longitude());
            final String ntpStatus = Optional.ofNullable(reference)
                .map(r -> String.format("NTP: %s (RTT: %dms, Stratum: %d)", r.time(), r.rttMilliseconds(), r.stratum()))
                .orElse("NTP: No Reference");
            final String healthMsg = String.format("[%s | GPS:%s | NTP:%s | Mode:%s]", 
                health.syncStatus(), health.gpsStatus(), health.ntpStatus(), health.mode());
            
            log.info("{} Fix: {} | {} | {}", healthMsg, data, ntpStatus, grid);
            // Logging Stability as Jitter for historical continuity, but including RMS for high-fidelity analysis
            log.info("Signal Quality: {} | Offset: {} | Jitter: {}us | RMS: {}us", 
                data.signalQuality(), precision.systemOffset(), precision.stabilityMicroseconds(), precision.rmsJitterMicroseconds());
        });
    }

    private void runWithContext(final Runnable action) {
        MDC.put("pulseId", pulseId);
        try {
            action.run();
        } finally {
            MDC.clear();
        }
    }
}
