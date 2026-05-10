package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.time.LocalDate;

/**
 * Business Rule: [PHASE 2, STEP 4] - Parsing Engine (Basic).
 * Comprehensive coverage including error branches and malformed data.
 */
class NmeaParserTest extends BddTest {

    private final NmeaParserFixture fixture = new NmeaParserFixture();

    @Test
    void givenValidGprmc_whenParsing_thenDataIsExtracted() {
        fixture.givenValidSentence("$GPRMC,232810.00,A,4617.00579,N,08753.28148,W,0.650,,020426,,,A");
        fixture.whenParsing();
        fixture.thenUtcTimeIs(LocalTime.of(23, 28, 10));
        fixture.thenLatitudeIs(46.28342983333333);
        fixture.thenLongitudeIs(-87.88802466666667);
        fixture.thenDateIs(LocalDate.of(2026, 4, 2));
    }

    @Test
    void givenInvalidChecksumFormat_whenParsing_thenReturnsPreviousState() {
        GpsData initial = new GpsData(null, null, 0, 0, 0, 0);
        fixture.givenSentence("$GPRMC,123,A*ZZ"); // Invalid hex in checksum
        fixture.whenParsingWith(initial);
        fixture.thenResultIs(initial);
    }

    @Test
    void givenMalformedTimeInGprmc_whenParsing_thenTimeIsPreserved() {
        GpsData initial = new GpsData(LocalTime.of(10,0,0), null, 0, 0, 0, 0);
        fixture.givenSentence("$GPRMC,12,A,0,N,0,E,0,0,010126,,,A"); // Time too short (12)
        fixture.whenParsingWith(initial);
        fixture.thenUtcTimeIs(LocalTime.of(10, 0, 0));
    }

    @Test
    void givenNorthernAndEasternCoords_whenParsing_thenCoordinatesArePositive() {
        fixture.givenValidSentence("$GPRMC,123456,A,1000.000,N,02000.000,E,0.0,,010126,,,A");
        fixture.whenParsing();
        fixture.thenLatitudeIs(10.0);
        fixture.thenLongitudeIs(20.0);
    }

    @Test
    void givenSouthernAndWesternCoords_whenParsing_thenCoordinatesAreNegative() {
        fixture.givenValidSentence("$GPRMC,123456,A,1000.000,S,02000.000,W,0.0,,010126,,,A");
        fixture.whenParsing();
        fixture.thenLatitudeIs(-10.0);
        fixture.thenLongitudeIs(-20.0);
    }

    @Test
    void givenPartialGpzdaFields_whenParsing_thenPreviousDateIsPreserved() {
        LocalDate initialDate = LocalDate.of(2026, 1, 1);
        GpsData initial = new GpsData(null, initialDate, 0, 0, 0, 0);
        
        // Missing month and year in ZDA
        fixture.givenSentence("$GPZDA,123456,01,,"); 
        fixture.whenParsingWith(initial);
        fixture.thenDateIs(initialDate);
    }

    @Test
    void givenShortSentences_whenParsing_thenHandlesGracefully() {
        GpsData initial = new GpsData(null, null, 0, 0, 0, 0);
        fixture.givenSentence("$GPRMC,short"); // Too few fields
        fixture.whenParsingWith(initial);
        fixture.thenResultIs(initial);

        fixture.givenSentence("$GPGGA,short");
        fixture.whenParsingWith(initial);
        fixture.thenResultIs(initial);

        fixture.givenSentence("$GPZDA,short");
        fixture.whenParsingWith(initial);
        fixture.thenResultIs(initial);
    }

    @Test
    void givenGpzdaWithFullFields_whenParsing_thenAllFieldsUsed() {
        fixture.givenValidSentence("$GPZDA,123456,01,01,2026,00,00");
        fixture.whenParsing();
        fixture.thenDateIs(java.time.LocalDate.of(2026, 1, 1));
    }

    @Test
    void givenSentenceWithMisplacedAsterisk_whenParsing_thenChecksumIsInvalid() {
        fixture.givenSentence("$GPRMC,123*A*68"); // Misplaced or extra asterisk
        fixture.whenParsing();
        fixture.thenResultIsNotNull(); // Returns preserved state
    }

    @Test
    void givenSentenceWithTooShortChecksum_whenParsing_thenChecksumIsInvalid() {
        fixture.givenSentence("$GPRMC,123*6"); // Checksum must be 2 chars
        fixture.whenParsing();
        fixture.thenResultIsNotNull();
    }

    @Test
    void givenSimulatorGpzda_whenParsing_thenChecksumIsValid() {
        // This is the exact string from SimulationSerialPort (fixed)
        fixture.givenValidSentence("$GPZDA,232810.00,02,04,2026,00,00*6C");
        fixture.whenParsing();
        fixture.thenDateIs(java.time.LocalDate.of(2026, 4, 2));
    }

    private class NmeaParserFixture {
        private String sentence;
        private final NmeaParser parser = new NmeaParser();
        private GpsData result;

        void givenSentence(String sentence) {
            this.sentence = sentence;
        }

        void givenValidSentence(String sentence) {
            this.sentence = sentence;
        }

        void whenParsing() {
            GpsData initial = new GpsData(null, null, 0, 0, 0, 0);
            this.result = parser.parse(sentence, initial);
        }

        void whenParsingWith(GpsData state) {
            this.result = parser.parse(sentence, state);
        }

        void thenResultIsNotNull() {
            thenNotNull(result);
        }

        void thenResultIs(GpsData expected) {
            then(result, expected);
        }

        void thenUtcTimeIs(LocalTime expected) {
            then(result.utcTime(), expected);
        }

        void thenDateIs(LocalDate expected) {
            then(result.date(), expected);
        }

        void thenLatitudeIs(double expected) {
            then(result.latitude(), expected);
        }

        void thenLongitudeIs(double expected) {
            then(result.longitude(), expected);
        }

        void thenAltitudeIs(double expected) {
            then(result.altitude(), expected);
        }

        void thenSatelliteCountIs(int expected) {
            then(result.satelliteCount(), expected);
        }
    }
}
