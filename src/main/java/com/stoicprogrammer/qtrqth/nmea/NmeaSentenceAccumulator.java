package com.stoicprogrammer.qtrqth.nmea;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Functional accumulator for NMEA sentences.
 * Uses a Predicate-driven Rule Engine to eliminate imperative branching.
 * Adheres to strict finality mandates.
 */
public final class NmeaSentenceAccumulator {
    private final StringBuilder buffer = new StringBuilder();

    private record Rule(boolean condition, Runnable action) {}
    private record IngestionRule(boolean condition, Supplier<Optional<String>> action) {}

    /**
     * Accumulates bytes into a complete NMEA sentence.
     * @param b The byte to add.
     * @return An Optional containing the sentence if finished, empty otherwise.
     */
    public Optional<String> process(final byte b) {
        final char c = (char) b;

        // Rule 1: Reset buffer on start of sentence
        List.of(
            new Rule(c == '$', () -> buffer.setLength(0)),
            new Rule(true, () -> {})
        ).stream()
         .filter(r -> r.condition)
         .findFirst()
         .ifPresent(r -> r.action.run());

        // Rule 2: Process character based on state
        return List.of(
            new IngestionRule(isBufferActive() && isLineTerminator(c), this::finalizeSentence),
            new IngestionRule(isBufferActive() || c == '$', () -> {
                buffer.append(c);
                return Optional.empty();
            }),
            new IngestionRule(true, Optional::empty)
        ).stream()
         .filter(r -> r.condition)
         .findFirst()
         .map(r -> r.action.get())
         .orElse(Optional.empty());
    }

    private boolean isBufferActive() {
        return buffer.length() > 0 && buffer.charAt(0) == '$';
    }

    private boolean isLineTerminator(final char c) {
        return c == '\n' || c == '\r';
    }

    private Optional<String> finalizeSentence() {
        final String sentence = buffer.toString().trim();
        buffer.setLength(0);
        // Use Optional.of to return the trimmed sentence if it started with $
        return Optional.of(sentence).filter(s -> s.startsWith("$"));
    }
}
