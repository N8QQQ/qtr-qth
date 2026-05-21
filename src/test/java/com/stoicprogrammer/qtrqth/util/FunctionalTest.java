package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for functional utility methods.
 * Adheres to strict AssertJ fluent assertion standards.
 */
class FunctionalTest extends BddTest {

    private static final int TEST_INT = 123;

    @ParameterizedTest
    @CsvSource({
        "123, 10, 123",
        " -456, 10, -456",
        " FF, 16, 255",
        " 0, 10, 0"
    })
    void should_parse_valid_integers(final String input, final int radix, final int expected) {
        assertThat(Functional.tryParseInt(input, radix)).contains(expected);
    }

    @Test
    void should_return_empty_for_malformed_integers() {
        assertThat(Functional.tryParseInt("not_a_number")).isEmpty();
        assertThat(Functional.tryParseInt("")).isEmpty();
        assertThat(Functional.tryParseInt(null)).isEmpty();
        assertThat(Functional.tryParseInt(" 12 3 ")).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "123.456, 123.456",
        "-0.001, -0.001",
        "1e3, 1000.0",
        " 42 , 42.0"
    })
    void should_parse_valid_doubles(final String input, final double expected) {
        assertThat(Functional.tryParseDouble(input)).contains(expected);
    }

    @Test
    void should_return_empty_for_malformed_doubles() {
        assertThat(Functional.tryParseDouble("invalid")).isEmpty();
        assertThat(Functional.tryParseDouble(null)).isEmpty();
    }

    @Test
    void should_wrap_throwing_function() {
        // Testing the wrapper logic with a checked exception scenario
        final java.util.function.Function<String, Integer> mapper = 
            Functional.wrap(s -> {
                if (s.equals("io-fail")) {
                    throw new IOException("Checked Error");
                }
                return Functional.tryParseInt(s).orElse(0);
            });
            
        assertThat(mapper.apply("123")).isEqualTo(TEST_INT);
    }

    @Test
    void should_throw_runtime_exception_on_wrapped_failure() {
        // Verifying that checked exceptions are pivoted to RuntimeException
        final java.util.function.Function<String, Integer> mapper =
            Functional.wrap(s -> {
                throw new IOException("Checked Error");
            });

        assertThatThrownBy(() -> mapper.apply("any"))
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(IOException.class);
    }
}
