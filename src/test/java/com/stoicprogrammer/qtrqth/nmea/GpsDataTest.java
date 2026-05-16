package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GpsData record.
 */
class GpsDataTest extends BddTest {

    private static final int HOUR = 12;
    private static final int MINUTE = 30;
    private static final int YEAR = 2026;
    private static final int MONTH = 4;
    private static final int DAY = 3;
    private static final double TEST_LAT = 45.0;
    private static final double TEST_LON = -90.0;
    private static final double TEST_ALT = 150.0;
    private static final int TEST_SATS = 8;

    private final GpsDataFixture fixture = new GpsDataFixture();

    @Test
    void should_create_immutable_gps_data_record() {
        fixture.given_data(LocalTime.of(HOUR, MINUTE, 0), LocalDate.of(YEAR, MONTH, DAY), TEST_LAT, TEST_LON, TEST_ALT, TEST_SATS);
        fixture.then_utc_time_is(LocalTime.of(HOUR, MINUTE, 0));
        fixture.then_latitude_is(TEST_LAT);
    }

    private final class GpsDataFixture {
        private GpsData data;

        void given_data(final LocalTime time, final LocalDate date, final double lat, final double lon, final double alt, final int sats) {
            this.data = new GpsData(time, date, lat, lon, alt, sats);
        }

        void then_utc_time_is(final LocalTime expected) {
            assertThat(data.utcTime()).isEqualTo(expected);
        }

        void then_latitude_is(final double expected) {
            assertThat(data.latitude()).isEqualTo(expected);
        }
    }
}
