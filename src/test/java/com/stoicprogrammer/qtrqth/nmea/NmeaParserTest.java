package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class NmeaParserTest extends BddTest {

    private static final int INITIAL_HOUR = 10;
    private static final int MOCK_HOUR = 12;
    private static final int MOCK_MIN = 34;
    private static final int MOCK_SEC = 56;
    private static final int MOCK_YEAR = 2026;
    private static final int MOCK_MONTH = 5;
    private static final int MOCK_DAY = 23;
    private static final int MOCK_SATS = 12;

    private static final double TEST_LAT = 46.283429;
    private static final double TEST_OFFSET = 0.0001;
    private static final double MOCK_PDOP = 1.5;
    private static final double MOCK_HDOP = 0.85;
    private static final double MOCK_VDOP = 1.2;
    private static final int MOCK_SAT_COUNT = 4;
    private static final int MOCK_PRN = 1;
    private static final int MOCK_ELEV = 40;
    private static final int MOCK_AZIM = 90;
    private static final int MOCK_SNR = 40;
    private static final double MOCK_LAT_STD = 1.2;
    private static final double MOCK_LON_STD = 1.5;
    private static final double MOCK_ALT_STD = 2.0;

    private final ParserFixture fixture = new ParserFixture();

    @Test
    void should_parse_valid_gprmc_sentence() {
        fixture.given_sentence("$GPRMC,123456,A,4617.00579,N,08753.28148,W,000.0,000.0,230526,,,A");
        fixture.when_parsed();
        fixture.then_time_is(MOCK_HOUR, MOCK_MIN, MOCK_SEC);
        fixture.then_date_is(MOCK_YEAR, MOCK_MONTH, MOCK_DAY);
        fixture.then_latitude_is(TEST_LAT);
    }

    @Test
    void should_parse_gpgll_position() {
        fixture.given_sentence("$GPGLL,4617.00579,N,08753.28148,W,123456,A");
        fixture.when_parsed();
        fixture.then_time_is(MOCK_HOUR, MOCK_MIN, MOCK_SEC);
        fixture.then_latitude_is(TEST_LAT);
    }

    @Test
    void should_parse_gpgsa_precision_and_active_sats() {
        fixture.given_sentence("$GPGSA,A,3,01,02,03,04,05,06,07,08,09,10,11,12,1.5,0.85,1.2");
        fixture.when_parsed();
        fixture.then_precision_is(MOCK_PDOP, MOCK_HDOP, MOCK_VDOP);
    }

    @Test
    void should_parse_gpgsv_satellites_in_view() {
        fixture.given_sentence("$GPGSV,2,1,08,01,40,090,40,02,20,140,35,03,50,270,30,04,10,320,25");
        fixture.when_parsed();
        fixture.then_satellite_in_view_count_is(MOCK_SAT_COUNT);
        fixture.then_satellite_details_are_accurate(0, MOCK_PRN, MOCK_ELEV, MOCK_AZIM, MOCK_SNR);
    }

    @Test
    void should_parse_gpgst_error_statistics() {
        fixture.given_sentence("$GPGST,123456,1.0,0.5,0.5,90.0,1.2,1.5,2.0");
        fixture.when_parsed();
        fixture.then_error_stats_are_accurate(MOCK_LAT_STD, MOCK_LON_STD, MOCK_ALT_STD);
    }

    @Test
    void should_parse_gngns_fix_data() {
        fixture.given_sentence("$GNGNS,123456,4617.00579,N,08753.28148,W,AN,12,1.2,425.1,M,-35.0,M,,");
        fixture.when_parsed();
        fixture.then_time_is(MOCK_HOUR, MOCK_MIN, MOCK_SEC);
        fixture.then_latitude_is(TEST_LAT);
        assertThat(fixture.result.satelliteCount()).isEqualTo(MOCK_SATS);
    }

    @Test
    void should_reject_sentence_with_invalid_checksum() {
        fixture.given_initial_state_with_time(INITIAL_HOUR, 0, 0);
        fixture.given_sentence("$GPRMC,123456,A,4617.0,N,08753.2,W,0.0,,230526,,,A*FF");
        fixture.when_parsed();
        fixture.then_time_is(INITIAL_HOUR, 0, 0); 
    }

    @Test
    void should_handle_truncated_sentences_gracefully() {
        fixture.given_initial_state_with_time(INITIAL_HOUR, 0, 0);
        fixture.given_sentence("$GPRMC,123");
        fixture.when_parsed();
        fixture.then_time_is(INITIAL_HOUR, 0, 0);
    }

    @Test
    void should_handle_empty_fields_in_valid_sentences() {
        fixture.given_sentence("$GPRMC,,A,,,,,,,230526,,,A");
        fixture.when_parsed();
        fixture.then_date_is(MOCK_YEAR, MOCK_MONTH, MOCK_DAY);
        assertThat(fixture.result.utcTime()).isNull();
    }

    @Test
    void should_identify_trigger_sentences() {
        final NmeaParser parser = new NmeaParser();
        assertThat(parser.isTrigger("$GPZDA,123456.00,23,05,2026,00,00")).isTrue();
        assertThat(parser.isTrigger("$GPRMC,123456,A")).isTrue();
        assertThat(parser.isTrigger("$GPGGA,123456,...")).isFalse();
    }

    private static final class ParserFixture {
        private final NmeaParser parser = new NmeaParser();
        private String sentence;
        private GpsData initial = GpsData.EMPTY;
        private GpsData result = GpsData.EMPTY;

        void given_sentence(final String s) {
            this.sentence = s;
        }

        void given_initial_state_with_time(final int h, final int m, final int s) {
            this.initial = new GpsData(LocalTime.of(h, m, s), null, 0, 0, 0, 0);
        }

        void when_parsed() {
            this.result = parser.parse(sentence, initial);
        }

        void then_time_is(final int h, final int m, final int s) {
            assertThat(result.utcTime()).isEqualTo(LocalTime.of(h, m, s));
        }

        void then_date_is(final int y, final int m, final int d) {
            assertThat(result.date()).isEqualTo(LocalDate.of(y, m, d));
        }

        void then_latitude_is(final double expected) {
            assertThat(result.latitude()).isCloseTo(expected, org.assertj.core.data.Offset.offset(TEST_OFFSET));
        }

        void then_precision_is(final double pdop, final double hdop, final double vdop) {
            assertThat(result.pdop()).isEqualTo(pdop);
            assertThat(result.hdop()).isEqualTo(hdop);
            assertThat(result.vdop()).isEqualTo(vdop);
        }

        void then_satellite_in_view_count_is(final int count) {
            assertThat(result.satellitesInView()).hasSize(count);
        }

        void then_satellite_details_are_accurate(final int index, final int prn, final int elev, final int azim, final int snr) {
            final GpsData.SatelliteFix sat = result.satellitesInView().get(index);
            assertThat(sat.prn()).isEqualTo(prn);
            assertThat(sat.elevation()).isEqualTo(elev);
            assertThat(sat.azimuth()).isEqualTo(azim);
            assertThat(sat.snr()).isEqualTo(snr);
        }

        void then_error_stats_are_accurate(final double lat, final double lon, final double alt) {
            assertThat(result.precisionStats().latStdDev()).isEqualTo(lat);
            assertThat(result.precisionStats().lonStdDev()).isEqualTo(lon);
            assertThat(result.precisionStats().altStdDev()).isEqualTo(alt);
        }
    }
}
