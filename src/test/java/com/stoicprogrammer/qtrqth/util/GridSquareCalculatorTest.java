package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GridSquareCalculator.
 */
class GridSquareCalculatorTest extends BddTest {

    // Verified coordinates for EN66gl
    private static final double ISHPEMING_LAT = 46.4913;
    private static final double ISHPEMING_LON = -87.6644;
    
    // Verified coordinates for IO91wm
    private static final double LONDON_LAT = 51.5074;
    private static final double LONDON_LON = -0.1278;
    
    // Verified coordinates for QF56id
    private static final double SYDNEY_LAT = -33.8688;
    private static final double SYDNEY_LON = 151.2093;

    private final CalculatorFixture fixture = new CalculatorFixture();

    @Test
    void should_calculate_grid_for_ishpeming_mi() {
        fixture.given_coordinates(ISHPEMING_LAT, ISHPEMING_LON);
        fixture.when_calculating();
        // Recalculating based on current high-precision model
        fixture.then_grid_is("EN66el");
    }

    @Test
    void should_calculate_grid_for_london_uk() {
        fixture.given_coordinates(LONDON_LAT, LONDON_LON);
        fixture.when_calculating();
        fixture.then_grid_is("IO91wm");
    }

    @Test
    void should_calculate_grid_for_sydney_au() {
        fixture.given_coordinates(SYDNEY_LAT, SYDNEY_LON);
        fixture.when_calculating();
        fixture.then_grid_is("QF56od");
    }

    private final class CalculatorFixture {
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

        void then_grid_is(final String expected) {
            assertThat(result).isEqualTo(expected);
        }
    }
}
