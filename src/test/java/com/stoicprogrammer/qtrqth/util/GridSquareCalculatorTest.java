package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Business Rule: [PHASE 1, STEP 4] - Location Context.
 */
class GridSquareCalculatorTest extends BddTest {

    private final CalculatorFixture fixture = new CalculatorFixture();

    @Test
    void given_coordinates_when_calculating_then_correct_grid_square_is_returned() {
        // Ishpeming, MI: 46.4913N, 87.6644W
        fixture.given_coordinates(46.4913, -87.6644);
        fixture.when_calculating();
        fixture.then_grid_square_is("EN66el");
    }

    @Test
    void given_london_coordinates_when_calculating_then_correct_grid_square_is_returned() {
        // London: 51.5074N, 0.1278W
        fixture.given_coordinates(51.5074, -0.1278);
        fixture.when_calculating();
        fixture.then_grid_square_is("IO91wm");
    }

    @Test
    void given_sydney_coordinates_when_calculating_then_correct_grid_square_is_returned() {
        // Sydney: 33.8688S, 151.2093E
        fixture.given_coordinates(-33.8688, 151.2093);
        fixture.when_calculating();
        fixture.then_grid_square_is("QF56od");
    }

    private class CalculatorFixture {
        private double latitude;
        private double longitude;
        private String result;

        void given_coordinates(final double lat, final double lon) {
            this.latitude = lat;
            this.longitude = lon;
        }

        void when_calculating() {
            this.result = GridSquareCalculator.calculate(latitude, longitude);
        }

        void then_grid_square_is(final String expected) {
            assertThat(result).isEqualTo(expected);
        }
    }
}
