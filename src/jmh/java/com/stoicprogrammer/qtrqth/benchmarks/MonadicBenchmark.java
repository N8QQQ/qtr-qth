package com.stoicprogrammer.qtrqth.benchmarks;

import io.vavr.control.Try;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class MonadicBenchmark {

    private final String validInt = "123";
    private final String validDouble = "123.456";
    private final String invalid = "not_a_number";

    @Benchmark
    @SuppressWarnings("java:S1166") // Raw JDK benchmark baseline
    public Optional<Integer> imperativeSuccess() {
        try {
            return Optional.of(Integer.parseInt(validInt));
        } catch (final NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Benchmark
    public Optional<Integer> vavrSuccess() {
        return Try.of(() -> Integer.parseInt(validInt)).toJavaOptional();
    }

    @Benchmark
    @SuppressWarnings("java:S1166") // Raw JDK benchmark baseline
    public Optional<Double> imperativeDoubleSuccess() {
        try {
            return Optional.of(Double.parseDouble(validDouble));
        } catch (final NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Benchmark
    public Optional<Double> vavrDoubleSuccess() {
        return Try.of(() -> Double.parseDouble(validDouble)).toJavaOptional();
    }

    @Benchmark
    @SuppressWarnings("java:S1166") // Raw JDK benchmark baseline
    public Optional<Integer> imperativeFailure() {
        try {
            return Optional.of(Integer.parseInt(invalid));
        } catch (final NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Benchmark
    public Optional<Integer> vavrFailureHandling() {
        return Try.of(() -> Integer.parseInt(invalid)).toJavaOptional();
    }
}
