package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Business Rule: [PHASE 2, STEP 3] - The "NMEA Nibbler" (Data Ingestion).
 */
class NmeaSentenceAccumulatorTest extends BddTest {

    private final AccumulatorFixture fixture = new AccumulatorFixture();

    @Test
    void given_raw_nmea_sentence_when_adding_byte_by_byte_then_full_sentence_is_returned() {
        fixture.given_raw_sentence("$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A\r\n");
        fixture.when_adding_bytes();
        fixture.then_last_sentence_was("$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A");
    }

    @Test
    void given_multiple_nmea_sentences_when_injected_sequentially_then_each_sentence_is_returned_correctly() {
        fixture.given_raw_sentence("$GPZDA,123456.00,01,01,2026,00,00*6D\r\n");
        fixture.when_adding_bytes();
        fixture.then_last_sentence_was("$GPZDA,123456.00,01,01,2026,00,00*6D");

        fixture.given_raw_sentence("$GPGGA,123456.00,4000.0000,N,08000.0000,W,1,08,0.9,100.0,M,-30.0,M,,*42\r\n");
        fixture.when_adding_bytes();
        fixture.then_last_sentence_was("$GPGGA,123456.00,4000.0000,N,08000.0000,W,1,08,0.9,100.0,M,-30.0,M,,*42");
    }

    private class AccumulatorFixture {
        private final NmeaSentenceAccumulator accumulator = new NmeaSentenceAccumulator();
        private String rawInput;
        private Optional<String> lastCaptured = Optional.empty();

        void given_raw_sentence(final String raw) {
            this.rawInput = raw;
        }

        void when_adding_bytes() {
            this.lastCaptured = rawInput.chars()
                .mapToObj(c -> accumulator.process((byte) c))
                .flatMap(Optional::stream)
                .reduce((first, second) -> second);
        }

        void then_last_sentence_was(final String expected) {
            assertThat(lastCaptured).isPresent().contains(expected);
        }
    }
}
