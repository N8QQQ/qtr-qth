package com.stoicprogrammer.qtrqth.util;

import io.vavr.collection.Vector;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * High-fidelity interpolation engine for high-rate telemetry simulation.
 * Massages 1Hz ground-truth NMEA into synthetic 25Hz/50Hz streams.
 */
public final class TelemetryInterpolationEngine {
    private static final int TYPE_START = 3;
    private static final int TYPE_END = 6;
    private static final int TIME_IDX_GLL = 5;
    private static final int TIME_IDX_DEFAULT = 1;
    private static final int TIME_STRING_MIN_LEN = 6;
    private static final int TIME_SECOND_START = 4;
    private static final int SCALE_PRECISION = 2;

    /**
     * Interpolates a list of 1Hz sentences into a high-rate stream.
     * Uses Epoch-Aware grouping to maintain realistic interleaving.
     */
    public List<String> interpolate(final List<String> sourceSentences, final int targetFrequency) {
        return Map.<Boolean, Supplier<List<String>>>of(
            true, () -> List.copyOf(sourceSentences),
            false, () -> performInterpolation(sourceSentences, targetFrequency)
        ).get(targetFrequency <= 1).get();
    }

    private List<String> performInterpolation(final List<String> sourceSentences, final int targetFrequency) {
        // 1. Group sentences into 1-second Epochs using a functional fold
        final List<List<String>> epochs = Vector.ofAll(sourceSentences)
            .foldLeft(Vector.<Vector<String>>empty(), (acc, s) -> 
                (acc.isEmpty() || s.contains(".00")) 
                    ? acc.append(Vector.of(s))
                    : acc.update(acc.size() - 1, acc.last().append(s))
            ).map(Vector::asJava)
            .toJavaList();

        // 2. Interpolate each epoch into 'targetFrequency' sub-bursts
        return epochs.stream()
            .flatMap(epoch -> IntStream.range(0, targetFrequency).boxed()
                .flatMap(step -> {
                    final java.math.BigDecimal fraction = java.math.BigDecimal.valueOf(step)
                            .divide(java.math.BigDecimal.valueOf(targetFrequency), SCALE_PRECISION, java.math.RoundingMode.HALF_UP);
                    return epoch.stream().map(sentence -> massageSentence(sentence, fraction));
                }))
            .toList();
    }

    private String massageSentence(final String sentence, final java.math.BigDecimal fractionOffset) {
        return Option.of(sentence)
            .filter(s -> !s.contains("TXT") && s.contains(","))
            .map(s -> {
                final String[] parts = s.split(",", -1);
                final String type = parts[0].length() >= TYPE_END ? parts[0].substring(TYPE_START, TYPE_END) : "";

                // Pure Functional Dispatch Table (Eliminating imperative branching for Technical Purity)
                final Map<String, Supplier<String>> dispatch = Map.of(
                    "ZDA", () -> updateTimestamp(parts, fractionOffset),
                    "RMC", () -> updateTimestamp(parts, fractionOffset),
                    "GGA", () -> updateTimestamp(parts, fractionOffset),
                    "GLL", () -> updateTimestamp(parts, fractionOffset)
                );

                return Option.of(dispatch.get(type))
                    .map(Supplier::get)
                    .getOrElse(s);
            })
            .getOrElse(sentence);
    }

    private String updateTimestamp(final String[] parts, final java.math.BigDecimal fractionOffset) {
        final int timeIdx = parts[0].endsWith("GLL") ? TIME_IDX_GLL : TIME_IDX_DEFAULT;
        
        return Option.of(parts)
            .filter(p -> p.length > timeIdx && p[timeIdx].length() >= TIME_STRING_MIN_LEN)
            .map(p -> Try.of(() -> {
                final String rawTime = p[timeIdx];
                final java.math.BigDecimal baseSeconds = new java.math.BigDecimal(rawTime.substring(TIME_SECOND_START));
                final java.math.BigDecimal massagedSeconds = baseSeconds.add(fractionOffset);
                
                final String newTime = String.format("%s%05.2f", rawTime.substring(0, TIME_SECOND_START), massagedSeconds.doubleValue());
                final String[] massagedParts = p.clone();
                massagedParts[timeIdx] = newTime;
                
                final String payload = String.join(",", massagedParts);
                final String cleanPayload = payload.contains("*") ? payload.substring(0, payload.lastIndexOf('*')) : payload;
                
                return cleanPayload + "*" + calculateChecksum(cleanPayload);
            }).getOrElse(String.join(",", p)))
            .getOrElse(String.join(",", parts));
    }

    private String calculateChecksum(final String sentence) {
        final String content = sentence.startsWith("$") ? sentence.substring(1) : sentence;
        final int checksum = content.chars().reduce(0, (a, b) -> a ^ b);
        return String.format("%02X", checksum).toUpperCase();
    }
}
