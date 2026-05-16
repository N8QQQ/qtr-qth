package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for NmeaSentenceAccumulator.
 */
class NmeaSentenceAccumulatorTest extends BddTest {

    private final AccumulatorFixture fixture = new AccumulatorFixture();

    @Test
    void should_accumulate_sentence_between_start_and_end_delimiters() {
        fixture.given_byte('$');
        fixture.given_byte('G');
        fixture.given_byte('P');
        fixture.when_processing_byte('\r');
        fixture.then_sentence_is("$GP");
    }

    @Test
    void should_ignore_bytes_before_start_delimiter() {
        fixture.given_byte('X');
        fixture.given_byte('$');
        fixture.given_byte('A');
        fixture.when_processing_byte('\r');
        fixture.then_sentence_is("$A");
    }

    @Test
    void should_reset_on_new_start_delimiter_before_completion() {
        fixture.given_byte('$');
        fixture.given_byte('A');
        fixture.given_byte('$');
        fixture.given_byte('B');
        fixture.when_processing_byte('\r');
        fixture.then_sentence_is("$B");
    }

    private static final class AccumulatorFixture {
        private final NmeaSentenceAccumulator accumulator = new NmeaSentenceAccumulator();
        private Optional<String> result = Optional.empty();

        void given_byte(final char c) {
            accumulator.process((byte) c);
        }

        void when_processing_byte(final char c) {
            result = accumulator.process((byte) c);
        }

        void then_sentence_is(final String expected) {
            assertThat(result).contains(expected);
        }
    }
}
