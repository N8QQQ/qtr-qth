package com.stoicprogrammer.qtrqth.util;

import io.vavr.control.Try;
import java.util.Optional;
import java.util.function.Function;

/**
 * High-fidelity functional wrappers and monadic utilities.
 * Adheres to strict branchless and expression-based mandates.
 */
public final class Functional {

    /**
     * Specialized functional interface for operations that may throw checked exceptions.
     */
    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    /**
     * Wraps a throwing function into a standard Function, pivoting to a RuntimeException on failure.
     * Useful for clean integration with Java Streams.
     */
    public static <T, R> Function<T, R> wrap(final ThrowingFunction<T, R, Exception> throwingFunction) {
        return t -> Try.of(() -> throwingFunction.apply(t)).getOrElseThrow(e -> new RuntimeException(e));
    }

    /**
     * Pure functional wrapper for Integer parsing.
     * @param s The string to parse.
     * @return An Optional containing the integer, or empty if malformed.
     */
    public static Optional<Integer> tryParseInt(final String s) {
        return tryParseInt(s, 10);
    }

    /**
     * Pure functional wrapper for Integer parsing with radix.
     */
    public static Optional<Integer> tryParseInt(final String s, final int radix) {
        return Optional.ofNullable(s)
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .flatMap(str -> Try.of(() -> Integer.parseInt(str, radix)).toJavaOptional());
    }

    /**
     * Pure functional wrapper for Double parsing.
     */
    public static Optional<Double> tryParseDouble(final String s) {
        return Optional.ofNullable(s)
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .flatMap(str -> Try.of(() -> Double.parseDouble(str)).toJavaOptional());
    }

    /**
     * Pure functional wrapper for Long parsing.
     */
    public static Optional<Long> tryParseLong(final String s) {
        return Optional.ofNullable(s)
            .map(String::trim)
            .filter(str -> !str.isEmpty())
            .flatMap(str -> Try.of(() -> Long.parseLong(str)).toJavaOptional());
    }

    private Functional() {
        // Utility Class - Instances forbidden by Section 2.
    }
}
