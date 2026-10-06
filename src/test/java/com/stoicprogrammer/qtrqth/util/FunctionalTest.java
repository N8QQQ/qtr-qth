package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for functional utility methods.
 * Adheres to strict AssertJ fluent assertion standards and Given-When-Then fixtures.
 */
class FunctionalTest extends BddTest {

    private static final int TEST_INT = 123;
    private static final int DEFAULT_RADIX = 10;

    private final FunctionalFixture fixture = new FunctionalFixture();

    @ParameterizedTest
    @CsvSource({
        "123, 10, 123",
        " -456, 10, -456",
        " FF, 16, 255",
        " 0, 10, 0"
    })
    void should_parse_valid_integers(final String input, final int radix, final int expected) {
        fixture.given_input_string(input);
        fixture.given_radix(radix);
        fixture.when_parsing_integer();
        fixture.then_parsed_integer_contains(expected);
    }

    @Test
    void should_return_empty_for_malformed_integers() {
        fixture.given_input_string("not_a_number");
        fixture.when_parsing_integer();
        fixture.then_parsed_integer_is_empty();

        fixture.given_input_string("");
        fixture.when_parsing_integer();
        fixture.then_parsed_integer_is_empty();

        fixture.given_input_string(null);
        fixture.when_parsing_integer();
        fixture.then_parsed_integer_is_empty();

        fixture.given_input_string(" 12 3 ");
        fixture.when_parsing_integer();
        fixture.then_parsed_integer_is_empty();
    }

    @ParameterizedTest
    @CsvSource({
        "123.456, 123.456",
        "-0.001, -0.001",
        "1e3, 1000.0",
        " 42 , 42.0"
    })
    void should_parse_valid_doubles(final String input, final double expected) {
        fixture.given_input_string(input);
        fixture.when_parsing_double();
        fixture.then_parsed_double_contains(expected);
    }

    @Test
    void should_return_empty_for_malformed_doubles() {
        fixture.given_input_string("invalid");
        fixture.when_parsing_double();
        fixture.then_parsed_double_is_empty();

        fixture.given_input_string(null);
        fixture.when_parsing_double();
        fixture.then_parsed_double_is_empty();
    }

    @Test
    void should_wrap_throwing_function() {
        fixture.given_throwing_mapper(s -> {
            if ("io-fail".equals(s)) {
                throw new IOException("Checked Error");
            }
            return Functional.tryParseInt(s).orElse(0);
        });
        fixture.when_applying_mapper("123");
        fixture.then_mapper_result_is(TEST_INT);
    }

    @Test
    void should_throw_runtime_exception_on_wrapped_failure() {
        fixture.given_throwing_mapper(s -> {
            throw new IOException("Checked Error");
        });
        fixture.then_applying_mapper_throws_runtime_exception("any", IOException.class);
    }

    private final class FunctionalFixture {
        private String input;
        private int radix = DEFAULT_RADIX;
        private Optional<Integer> parsedInt;
        private Optional<Double> parsedDouble;
        private Function<String, Integer> mapper;
        private int mappedResult;

        void given_input_string(final String str) {
            this.input = str;
        }

        void given_radix(final int r) {
            this.radix = r;
        }

        void given_throwing_mapper(final CheckedFunction<String, Integer> checkedFn) {
            this.mapper = Functional.wrap(checkedFn::apply);
        }

        void when_parsing_integer() {
            this.parsedInt = Functional.tryParseInt(input, radix);
        }

        void when_parsing_double() {
            this.parsedDouble = Functional.tryParseDouble(input);
        }

        void when_applying_mapper(final String arg) {
            this.mappedResult = mapper.apply(arg);
        }

        void then_parsed_integer_contains(final int expected) {
            assertThat(parsedInt).contains(expected);
        }

        void then_parsed_integer_is_empty() {
            assertThat(parsedInt).isEmpty();
        }

        void then_parsed_double_contains(final double expected) {
            assertThat(parsedDouble).contains(expected);
        }

        void then_parsed_double_is_empty() {
            assertThat(parsedDouble).isEmpty();
        }

        void then_mapper_result_is(final int expected) {
            assertThat(mappedResult).isEqualTo(expected);
        }

        void then_applying_mapper_throws_runtime_exception(final String arg, final Class<? extends Throwable> causeClass) {
            assertThatThrownBy(() -> mapper.apply(arg))
                .isInstanceOf(RuntimeException.class)
                .hasCauseInstanceOf(causeClass);
        }
    }

    @FunctionalInterface
    private interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }
}
