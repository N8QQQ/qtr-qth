package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

/**
 * Business Rule: [PHASE 1, STEP 4] - Location Context.
 */
class GridSquareCalculatorTest extends BddTest {

    private final CalculatorFixture fixture = new CalculatorFixture();

    @Test
    void givenCoordinates_whenCalculating_thenCorrectGridSquareIsReturned() {
        // Ishpeming, MI: 46.4913N, 87.6644W
        fixture.givenCoordinates(46.4913, -87.6644);
        fixture.whenCalculating();
        fixture.thenGridSquareIs("EN66gl");
    }

    @Test
    void givenLondonCoordinates_whenCalculating_thenCorrectGridSquareIsReturned() {
        // London: 51.5074N, 0.1278W
        fixture.givenCoordinates(51.5074, -0.1278);
        fixture.whenCalculating();
        fixture.thenGridSquareIs("IO91wm");
    }

    @Test
    void givenSydneyCoordinates_whenCalculating_thenCorrectGridSquareIsReturned() {
        // Sydney: 33.8688S, 151.2093E
        fixture.givenCoordinates(-33.8688, 151.2093);
        fixture.whenCalculating();
        fixture.thenGridSquareIs("QF56id");
    }

    private class CalculatorFixture {
        private double latitude;
        private double longitude;
        private String result;

        void givenCoordinates(final double lat, final double lon) {
            this.latitude = lat;
            this.longitude = lon;
        }

        void whenCalculating() {
            this.result = GridSquareCalculator.calculate(latitude, longitude);
        }

        void thenGridSquareIs(final String expected) {
            then(result, expected);
        }
    }
}
