package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

/**
 * Business Rule: [PHASE 2, STEP 5] - Grid Square Calculation.
 */
class GridSquareCalculatorTest extends BddTest {

    private final GridFixture fixture = new GridFixture();

    @Test
    void givenSampleCoordinates_whenCalculating_thenCorrectGridSquareReturned() {
        // Test with a known location (New York City area)
        fixture.givenCoordinates(40.7128, -74.0060);
        fixture.whenCalculating();
        fixture.thenGridSquareIs("FN20xr");
    }

    @Test
    void givenAnotherLocation_whenCalculating_thenCorrectGridSquareReturned() {
        // Test with another known location (London)
        fixture.givenCoordinates(51.5074, -0.1278);
        fixture.whenCalculating();
        fixture.thenGridSquareIs("IO91wm");
    }

    private class GridFixture {
        private double lat;
        private double lon;
        private String result;

        void givenCoordinates(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        void whenCalculating() {
            this.result = GridSquareCalculator.calculate(lat, lon);
        }

        void thenGridSquareIs(String expected) {
            then(result, expected);
        }
    }
}
