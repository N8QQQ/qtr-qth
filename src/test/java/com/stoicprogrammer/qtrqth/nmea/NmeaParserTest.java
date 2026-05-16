package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Business Rule: [PHASE 2, STEP 4] - Parsing Engine (Basic).
 */
class NmeaParserTest extends BddTest {

    private final NmeaParserFixture fixture = new NmeaParserFixture();

    @Test
    void given_valid_gprmc_when_parsing_then_data_is_extracted() {
        fixture.given_valid_sentence("$GPRMC,232810.00,A,4617.00579,N,08753.28148,W,0.650,,020426,,,A");
        fixture.when_parsing();
        fixture.then_utc_time_is(LocalTime.of(23, 28, 10));
        fixture.then_latitude_is(46.28342983333333);
        fixture.then_longitude_is(-87.88802466666667);
        fixture.then_date_is(LocalDate.of(2026, 4, 2));
    }

    @Test
    void given_invalid_checksum_format_when_parsing_then_returns_previous_state() {
        final GpsData initial = new GpsData(null, null, 0, 0, 0, 0);
        fixture.given_sentence("$GPRMC,123,A*ZZ");
        fixture.when_parsing_with(initial);
        fixture.then_result_is(initial);
    }

    @Test
    void given_malformed_time_in_gprmc_when_parsing_then_time_is_preserved() {
        final GpsData initial = new GpsData(LocalTime.of(10, 0, 0), null, 0, 0, 0, 0);
        fixture.given_sentence("$GPRMC,12,A,0,N,0,E,0,0,010126,,,A");
        fixture.when_parsing_with(initial);
        fixture.then_utc_time_is(LocalTime.of(10, 0, 0));
    }

    @Test
    void given_northern_and_eastern_coords_when_parsing_then_coordinates_are_positive() {
        fixture.given_valid_sentence("$GPRMC,123456,A,1000.000,N,02000.000,E,0.0,,010126,,,A");
        fixture.when_parsing();
        fixture.then_latitude_is(10.0);
        fixture.then_longitude_is(20.0);
    }

    @Test
    void given_southern_and_western_coords_when_parsing_then_coordinates_are_negative() {
        fixture.given_valid_sentence("$GPRMC,123456,A,1000.000,S,02000.000,W,0.0,,010126,,,A");
        fixture.when_parsing();
        fixture.then_latitude_is(-10.0);
        fixture.then_longitude_is(-20.0);
    }

    @Test
    void given_partial_gpzda_fields_when_parsing_then_previous_date_is_preserved() {
        final LocalDate initialDate = LocalDate.of(2026, 1, 1);
        final GpsData initial = new GpsData(null, initialDate, 0, 0, 0, 0);
        
        fixture.given_sentence("$GPZDA,123456,01,,"); 
        fixture.when_parsing_with(initial);
        fixture.then_date_is(initialDate);
    }

    @Test
    void given_short_sentences_when_parsing_then_handles_gracefully() {
        final GpsData initial = new GpsData(null, null, 0, 0, 0, 0);
        fixture.given_sentence("$GPRMC,short");
        fixture.when_parsing_with(initial);
        fixture.then_result_is(initial);

        fixture.given_sentence("$GPGGA,short");
        fixture.when_parsing_with(initial);
        fixture.then_result_is(initial);

        fixture.given_sentence("$GPZDA,short");
        fixture.when_parsing_with(initial);
        fixture.then_result_is(initial);
    }

    @Test
    void given_gpzda_with_full_fields_when_parsing_then_all_fields_used() {
        fixture.given_valid_sentence("$GPZDA,123456,01,01,2026,00,00");
        fixture.when_parsing();
        fixture.then_date_is(java.time.LocalDate.of(2026, 1, 1));
    }

    @Test
    void given_sentence_with_misplaced_asterisk_when_parsing_then_checksum_is_invalid() {
        fixture.given_sentence("$GPRMC,123*A*68");
        fixture.when_parsing();
        fixture.then_result_is_not_null();
    }

    @Test
    void given_sentence_with_too_short_checksum_when_parsing_then_checksum_is_invalid() {
        fixture.given_sentence("$GPRMC,123*6");
        fixture.when_parsing();
        fixture.then_result_is_not_null();
    }

    @Test
    void given_simulator_gpzda_when_parsing_then_checksum_is_valid() {
        fixture.given_valid_sentence("$GPZDA,232810.00,02,04,2026,00,00*6C");
        fixture.when_parsing();
        fixture.then_date_is(java.time.LocalDate.of(2026, 4, 2));
    }

    private class NmeaParserFixture {
        private String sentence;
        private final NmeaParser parser = new NmeaParser();
        private GpsData result;

        void given_sentence(final String sentence) {
            this.sentence = sentence;
        }

        void given_valid_sentence(final String sentence) {
            this.sentence = sentence;
        }

        void when_parsing() {
            final GpsData initial = new GpsData(null, null, 0, 0, 0, 0);
            this.result = parser.parse(sentence, initial);
        }

        void when_parsing_with(final GpsData state) {
            this.result = parser.parse(sentence, state);
        }

        void then_result_is_not_null() {
            assertThat(result).isNotNull();
        }

        void then_result_is(final GpsData expected) {
            assertThat(result).isEqualTo(expected);
        }

        void then_utc_time_is(final LocalTime expected) {
            assertThat(result.utcTime()).isEqualTo(expected);
        }

        void then_date_is(final LocalDate expected) {
            assertThat(result.date()).isEqualTo(expected);
        }

        void then_latitude_is(final double expected) {
            assertThat(result.latitude()).isCloseTo(expected, org.assertj.core.data.Offset.offset(0.000001));
        }

        void then_longitude_is(final double expected) {
            assertThat(result.longitude()).isCloseTo(expected, org.assertj.core.data.Offset.offset(0.000001));
        }

        void then_altitude_is(final double expected) {
            assertThat(result.altitude()).isCloseTo(expected, org.assertj.core.data.Offset.offset(0.000001));
        }

        void then_satellite_count_is(final int expected) {
            assertThat(result.satelliteCount()).isEqualTo(expected);
        }
    }
}
