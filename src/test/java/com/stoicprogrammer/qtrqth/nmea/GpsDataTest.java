package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Business Rule: [PHASE 2, STEP 4] - Data Formatting.
 */
class GpsDataTest extends BddTest {

    private final GpsDataFixture fixture = new GpsDataFixture();

    @Test
    void given_gps_data_with_zero_seconds_when_formatting_to_string_then_seconds_are_still_displayed() {
        fixture.given_time(12, 30, 0);
        fixture.given_date(2026, 4, 3);
        fixture.when_formatting();
        fixture.then_result_contains("UTC: 12:30:00");
    }

    private class GpsDataFixture {
        private LocalTime time;
        private LocalDate date;
        private String result;

        void given_time(final int h, final int m, final int s) {
            this.time = LocalTime.of(h, m, s);
        }

        void given_date(final int y, final int m, final int d) {
            this.date = LocalDate.of(y, m, d);
        }

        void when_formatting() {
            final GpsData data = new GpsData(time, date, 0, 0, 0, 0);
            this.result = data.toString();
        }

        void then_result_contains(final String expected) {
            assertThat(result).contains(expected);
        }
    }
}
