package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Business Rule: [PHASE 2, STEP 4] - Data Formatting.
 * Ensure GPS data is presented clearly, with high-precision time (always showing seconds).
 */
class GpsDataTest extends BddTest {

    private final GpsDataFixture fixture = new GpsDataFixture();

    @Test
    void givenGpsDataWithZeroSeconds_whenFormattingToString_thenSecondsAreStillDisplayed() {
        fixture.givenTime(12, 30, 0);
        fixture.givenDate(2026, 4, 3);
        fixture.whenFormatting();
        fixture.thenResultContains("UTC: 12:30:00");
    }

    private class GpsDataFixture {
        private LocalTime time;
        private LocalDate date;
        private String result;

        void givenTime(final int h, final int m, final int s) {
            this.time = LocalTime.of(h, m, s);
        }

        void givenDate(final int y, final int m, final int d) {
            this.date = LocalDate.of(y, m, d);
        }

        void whenFormatting() {
            final GpsData data = new GpsData(time, date, 0, 0, 0, 0);
            this.result = data.toString();
        }

        void thenResultContains(final String expected) {
            thenTrue(result.contains(expected));
        }
    }
}
