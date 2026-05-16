package com.stoicprogrammer.qtrqth.benchmarks;

import io.vavr.control.Try;
import org.openjdk.jmh.annotations.*;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Performance Sentinel: Comparing Native vs Vavr monadic pipelines.
 * Measures the 'Galvanic' cost of our architectural abstractions.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class MonadicBenchmark {

    private final String validInt = "1234";
    private final String validDouble = "4617.00579";
    private final String invalid = "NOT_A_NUMBER";

    @Benchmark
    public Optional<Integer> nativeIntParsing() {
        try {
            return Optional.of(Integer.parseInt(validInt));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Benchmark
    public Optional<Integer> vavrIntParsing() {
        return Try.of(() -> Integer.parseInt(validInt)).toJavaOptional();
    }

    @Benchmark
    public Optional<Double> nativeDoubleParsing() {
        try {
            return Optional.of(Double.parseDouble(validDouble));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Benchmark
    public Optional<Double> vavrDoubleParsing() {
        return Try.of(() -> Double.parseDouble(validDouble)).toJavaOptional();
    }

    @Benchmark
    public Optional<Integer> nativeFailureHandling() {
        try {
            return Optional.of(Integer.parseInt(invalid));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Benchmark
    public Optional<Integer> vavrFailureHandling() {
        return Try.of(() -> Integer.parseInt(invalid)).toJavaOptional();
    }
}
