package com.stoicprogrammer.qtrqth.util;

import java.util.function.Function;

/**
 * Lightweight functional wrappers for Vanilla Java 8+.
 * Adheres to Phase 5 Architectural Rule 4.
 */
public final class Functional {

    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    /**
     * Wraps a function that throws checked exceptions into a standard Function.
     */
    public static <T, R> Function<T, R> wrap(final ThrowingFunction<T, R, Exception> throwingFunction) {
        return t -> {
            try {
                return throwingFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private Functional() {
        // Utility Class
    }
}
