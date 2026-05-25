package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import io.vavr.control.Try;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * High-fidelity interpolation engine for high-rate telemetry simulation.
 * Massages 1Hz ground-truth NMEA into synthetic 25Hz/50Hz streams.
 */
public final class TelemetryInterpolationEngine {
    private static final int HEX_RADIX = 16;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss.SS");

    /**
     * Interpolates a list of 1Hz sentences into a high-rate stream.
     * Uses Epoch-Aware grouping to maintain realistic interleaving.
     */
    public List<String> interpolate(final List<String> sourceSentences, final int targetFrequency) {
        if (targetFrequency <= 1) return List.copyOf(sourceSentences);

        final List<String> result = new ArrayList<>();
        
        // 1. Group sentences into 1-second Epochs
        // (A new epoch starts when we see a sentence with a .00 timestamp)
        final List<List<String>> epochs = new ArrayList<>();
        List<String> currentEpoch = new ArrayList<>();
        
        for (final String s : sourceSentences) {
            if (s.contains(".00") && !currentEpoch.isEmpty()) {
                epochs.add(List.copyOf(currentEpoch));
                currentEpoch = new ArrayList<>();
            }
            currentEpoch.add(s);
        }
        if (!currentEpoch.isEmpty()) epochs.add(currentEpoch);

        // 2. Interpolate each epoch into 'targetFrequency' sub-bursts
        epochs.forEach(epoch -> {
            IntStream.range(0, targetFrequency).forEach(step -> {
                final java.math.BigDecimal fraction = java.math.BigDecimal.valueOf(step)
                        .divide(java.math.BigDecimal.valueOf(targetFrequency), 2, java.math.RoundingMode.HALF_UP);
                epoch.forEach(sentence -> result.add(massageSentence(sentence, fraction)));
            });
        });

        return List.copyOf(result);
    }

    private String massageSentence(final String sentence, final java.math.BigDecimal fractionOffset) {
        // Support for informational sentences (like TXT) - pass through unchanged
        if (sentence.contains("TXT")) return sentence;
        if (!sentence.contains(",")) return sentence;
        
        final String[] parts = sentence.split(",", -1);
        final String type = parts[0].length() >= 6 ? parts[0].substring(3, 6) : "";

        return switch (type) {
            case "ZDA", "RMC", "GGA", "GLL" -> updateTimestamp(parts, fractionOffset);
            default -> sentence; // GSA, GSV, etc. stay same as they apply to the epoch
        };
    }

    private String updateTimestamp(final String[] parts, final java.math.BigDecimal fractionOffset) {
        final int timeIdx = parts[0].endsWith("GLL") ? 5 : 1;
        if (parts.length <= timeIdx || parts[timeIdx].length() < 6) return String.join(",", parts);

        final String rawTime = parts[timeIdx];
        return Try.of(() -> {
            final java.math.BigDecimal baseSeconds = new java.math.BigDecimal(rawTime.substring(4));
            final java.math.BigDecimal massagedSeconds = baseSeconds.add(fractionOffset);
            
            final String newTime = String.format("%s%05.2f", rawTime.substring(0, 4), massagedSeconds.doubleValue());
            final String[] massagedParts = parts.clone();
            massagedParts[timeIdx] = newTime;
            
            final String payload = String.join(",", massagedParts);
            final String cleanPayload = payload.contains("*") ? payload.substring(0, payload.lastIndexOf('*')) : payload;
            
            return cleanPayload + "*" + calculateChecksum(cleanPayload);
        }).getOrElse(String.join(",", parts));
    }

    private String calculateChecksum(final String sentence) {
        final String content = sentence.startsWith("$") ? sentence.substring(1) : sentence;
        final int checksum = content.chars().reduce(0, (a, b) -> a ^ b);
        return String.format("%02X", checksum).toUpperCase();
    }
}
