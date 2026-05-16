package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class FunctionalTest extends BddTest {

    private static final int TEST_INT = 123;
    private static final int TEST_INT_HEX = 255;
    private static final int RADIX_HEX = 16;
    private static final int TEST_INT_MAPPED = 456;

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
        final java.util.function.Function<String, Integer> mapper = Functional.wrap(s -> Integer.parseInt(s.trim()));
        assertThat(mapper.apply("123")).isEqualTo(TEST_INT);
        assertThat(mapper.apply("  456  ")).isEqualTo(TEST_INT_MAPPED);
    }

    @Test
    void should_throw_runtime_exception_on_wrapped_failure() {
        final java.util.function.Function<String, Integer> mapper = Functional.wrap(Integer::parseInt);
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> mapper.apply("fail")))
            .hasCauseInstanceOf(NumberFormatException.class);
    }
}
