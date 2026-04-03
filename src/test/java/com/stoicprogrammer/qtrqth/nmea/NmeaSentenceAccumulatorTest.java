package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

/**
 * Business Rule: [PHASE 2, STEP 3] - The "NMEA Nibbler" (Data Ingestion).
 */
class NmeaSentenceAccumulatorTest extends BddTest {

    private final AccumulatorFixture fixture = new AccumulatorFixture();

    @Test
    void givenARawNmeaSentence_whenAddingByteByByte_thenFullSentenceIsReturned() {
        fixture.givenRawSentence("$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A\r\n");
        fixture.whenAddingBytes();
        fixture.thenLastSentenceWas("$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A");
    }

    @Test
    void givenMultipleNmeaSentences_whenInjectedSequentially_thenEachSentenceIsReturnedCorrectly() {
        fixture.givenRawSentence("$GPZDA,123456.00,01,01,2026,00,00*6D\r\n");
        fixture.whenAddingBytes();
        fixture.thenLastSentenceWas("$GPZDA,123456.00,01,01,2026,00,00*6D");

        fixture.givenRawSentence("$GPGGA,123456.00,4000.0000,N,08000.0000,W,1,08,0.9,100.0,M,-30.0,M,,*42\r\n");
        fixture.whenAddingBytes();
        fixture.thenLastSentenceWas("$GPGGA,123456.00,4000.0000,N,08000.0000,W,1,08,0.9,100.0,M,-30.0,M,,*42");
    }

    @Test
    void givenNoiseBeforeSentence_whenAddingBytes_thenValidSentenceStillCaptured() {
        fixture.givenRawSentence("noise$GPRMC,123,A*66\r\n");
        fixture.whenAddingBytes();
        fixture.thenLastSentenceWas("$GPRMC,123,A*66");
    }

    @Test
    void givenIncompleteSentence_whenAddingBytes_thenReturnsNull() {
        fixture.givenRawSentence("$GPRMC,incomplete");
        fixture.whenAddingBytes();
        fixture.thenNoSentenceCaptured();
    }

    private class AccumulatorFixture {
        private final NmeaSentenceAccumulator accumulator = new NmeaSentenceAccumulator();
        private String rawInput;
        private String lastCaptured;

        void givenRawSentence(String raw) {
            this.rawInput = raw;
            this.lastCaptured = null;
        }

        void whenAddingBytes() {
            for (char c : rawInput.toCharArray()) {
                String result = accumulator.addByte((byte) c);
                if (result != null) {
                    lastCaptured = result;
                }
            }
        }

        void thenLastSentenceWas(String expected) {
            then(lastCaptured, expected);
        }

        void thenNoSentenceCaptured() {
            then(lastCaptured, null);
        }
    }
}
