package com.stoicprogrammer.qtrqth.benchmarks;

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
    public Optional<Integer> functionalIntSuccess() {
        return com.stoicprogrammer.qtrqth.util.Functional.tryParseInt(validInt);
    }

    @Benchmark
    public Optional<Double> functionalDoubleSuccess() {
        return com.stoicprogrammer.qtrqth.util.Functional.tryParseDouble(validDouble);
    }

    @Benchmark
    public Optional<Integer> functionalIntFailure() {
        return com.stoicprogrammer.qtrqth.util.Functional.tryParseInt(invalid);
    }
}
