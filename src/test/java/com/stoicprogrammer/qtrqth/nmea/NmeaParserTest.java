package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;

/**
 * Business Rule: [PHASE 2, STEP 4] - Parsing Engine (Basic).
 * Extract UTC time and date from $GPRMC or $GPZDA.
 */
class NmeaParserTest extends BddTest {

    private final NmeaParserFixture fixture = new NmeaParserFixture();

    @Test
    void givenAValidGprmcSentence_whenParsingTime_thenUtcTimeIsCorrectlyExtracted() {
        fixture.givenValidGprmcSentence("$GPRMC,232810.00,A,4617.00579,N,08753.28148,W,0.650,,020426,,,A");
        fixture.whenParsing();
        fixture.thenUtcTimeIs(LocalTime.of(23, 28, 10));
    }

    @Test
    void givenAValidGprmcSentence_whenParsingLocation_thenLatitudeAndLongitudeAreExtracted() {
        fixture.givenValidGprmcSentence("$GPRMC,232810.00,A,4617.00579,N,08753.28148,W,0.650,,020426,,,A");
        fixture.whenParsing();
        fixture.thenLatitudeIs(46.28342983333333); 
        fixture.thenLongitudeIs(-87.88802466666667);
    }

    @Test
    void givenInvalidSentence_whenParsing_thenReturnsNull() {
        fixture.givenInvalidSentence("NOT_NMEA");
        fixture.whenParsing();
        fixture.thenResultIsNull();
    }

    @Test
    void givenEmptyGprmcFields_whenParsing_thenHandlesGracefully() {
        fixture.givenValidGprmcSentence("$GPRMC,,V,,,,,,,,");
        fixture.whenParsing();
        fixture.thenResultIsNotNull();
        fixture.thenUtcTimeIsNull();
    }

    @Test
    void givenSouthernAndWesternCoords_whenParsing_thenCoordinatesAreNegative() {
        fixture.givenValidGprmcSentence("$GPRMC,123456,A,1000.000,S,02000.000,W,0.0,,010126,,,A");
        fixture.whenParsing();
        fixture.thenLatitudeIs(-10.0);
        fixture.thenLongitudeIs(-20.0);
    }

    @Test
    void givenAGpggaSentence_whenParsing_thenAltitudeAndSatCountAreExtracted() {
        fixture.givenValidSentence("$GPGGA,232810.00,4617.00579,N,08753.28148,W,1,07,1.15,431.1,M,-35.0,M,,");
        fixture.whenParsing();
        fixture.thenAltitudeIs(431.1);
        fixture.thenSatelliteCountIs(7);
    }

    @Test
    void givenAGpzdaSentence_whenParsing_thenDateIsExtracted() {
        fixture.givenValidSentence("$GPZDA,232810.00,02,04,2026,00,00");
        fixture.whenParsing();
        fixture.thenDateIs(java.time.LocalDate.of(2026, 4, 2));
    }

    @Test
    void givenInvalidChecksum_whenParsing_thenReturnsNull() {
        fixture.givenInvalidChecksum("$GPRMC,123456,A,4000.000,N,08000.000,W,0,0,010126,,,A*FF");
        fixture.whenParsing();
        fixture.thenResultIsNull();
    }

    @Test
    void givenValidChecksum_whenParsing_thenReturnsData() {
        // Verified sentence from live VFAN logs
        fixture.givenValidSentence("$GPRMC,232810.00,A,4617.00579,N,08753.28148,W,0.650,,020426,,,A*68");
        fixture.whenParsing();
        fixture.thenResultIsNotNull();
    }

    private class NmeaParserFixture {
        private String sentence;
        private final NmeaParser parser = new NmeaParser();
        private GpsData result;

        void givenInvalidChecksum(String sentence) {
            this.sentence = sentence;
        }

        void givenValidGprmcSentence(String sentence) {
            this.sentence = sentence;
        }

        void givenValidSentence(String sentence) {
            this.sentence = sentence;
        }

        void givenInvalidSentence(String sentence) {
            this.sentence = sentence;
        }

        void whenParsing() {
            this.result = parser.parse(sentence);
        }

        void thenResultIsNull() {
            then(result, null);
        }

        void thenResultIsNotNull() {
            thenNotNull(result);
        }

        void thenUtcTimeIsNull() {
            then(result.getUtcTime(), null);
        }

        void thenUtcTimeIs(LocalTime expected) {
            then(result.getUtcTime(), expected);
        }

        void thenDateIs(java.time.LocalDate expected) {
            then(result.getDate(), expected);
        }

        void thenLatitudeIs(double expected) {
            then(result.getLatitude(), expected);
        }

        void thenLongitudeIs(double expected) {
            then(result.getLongitude(), expected);
        }

        void thenAltitudeIs(double expected) {
            then(result.getAltitude(), expected);
        }

        void thenSatelliteCountIs(int expected) {
            then(result.getSatelliteCount(), expected);
        }
    }
}
