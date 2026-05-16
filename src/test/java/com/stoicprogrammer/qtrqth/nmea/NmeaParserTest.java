package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Business Rule: [PHASE 2, STEP 4] - Parsing Engine (Basic).
 * Hardened in Phase 6.6 with Test Data Vaulting.
 */
class NmeaParserTest extends BddTest {

    private final NmeaParserFixture fixture = new NmeaParserFixture();

    @Test
    void should_parse_valid_shack_sample_from_vault() {
        getTelemetrySentences("shack_sample_01.nmea").forEach(sentence -> {
            fixture.given_sentence(sentence);
            fixture.when_parsing_and_merging();
        });

        fixture.then_utc_time_is(LocalTime.of(23, 28, 10));
        fixture.then_latitude_is(46.28342983333333);
        fixture.then_longitude_is(-87.88802466666667);
        fixture.then_date_is(LocalDate.of(2026, 4, 2));
    }

    @Test
    void should_handle_temporal_and_positional_boundaries_from_vault() {
        // Boundary Sample: Midnight rollover, Date Line, Leap Year, North Pole
        getTelemetrySentences("boundary_stress_sample.nmea").forEach(sentence -> {
            fixture.given_sentence(sentence);
            fixture.when_parsing_and_merging();
        });

        // Final fix in stress sample is at North Pole on Feb 29, 2024
        fixture.then_date_is(LocalDate.of(2024, 2, 29));
        fixture.then_latitude_is(89.99998333333333);
        fixture.then_utc_time_is(LocalTime.of(12, 0, 0));
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
        fixture.given_sentence("$GPRMC,123456,A,1000.000,N,02000.000,E,0.0,,010126,,,A");
        fixture.when_parsing();
        fixture.then_latitude_is(10.0);
        fixture.then_longitude_is(20.0);
    }

    @Test
    void given_southern_and_western_coords_when_parsing_then_coordinates_are_negative() {
        fixture.given_sentence("$GPRMC,123456,A,1000.000,S,02000.000,W,0.0,,010126,,,A");
        fixture.when_parsing();
        fixture.then_latitude_is(-10.0);
        fixture.then_longitude_is(-20.0);
    }

    @Test
    void given_short_sentences_when_parsing_then_handles_gracefully() {
        final GpsData initial = new GpsData(null, null, 0, 0, 0, 0);
        fixture.given_sentence("$GPRMC,short");
        fixture.when_parsing_with(initial);
        fixture.then_result_is(initial);
    }

    @Test
    void should_validate_checksum_on_full_zda_sentence() {
        fixture.given_sentence("$GPZDA,232810.00,02,04,2026,00,00*6C");
        fixture.when_parsing();
        fixture.then_date_is(LocalDate.of(2026, 4, 2));
    }

    private class NmeaParserFixture {
        private String sentence;
        private final NmeaParser parser = new NmeaParser();
        private GpsData result = new GpsData(null, null, 0, 0, 0, 0);

        void given_sentence(final String sentence) {
            this.sentence = sentence;
        }

        void when_parsing() {
            final GpsData initial = new GpsData(null, null, 0, 0, 0, 0);
            this.result = parser.parse(sentence, initial);
        }

        void when_parsing_and_merging() {
            this.result = parser.parse(sentence, result);
        }

        void when_parsing_with(final GpsData state) {
            this.result = parser.parse(sentence, state);
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
    }
}
