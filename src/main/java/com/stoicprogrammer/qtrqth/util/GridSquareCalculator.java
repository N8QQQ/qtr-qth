package com.stoicprogrammer.qtrqth.util;

/**
 * Calculates Maidenhead Grid Squares from Latitude and Longitude.
 * Refactored for expression-based calculation and strict finality.
 */
public final class GridSquareCalculator {

    // Mathematical Constants
    private static final int LON_FIRST_DIVISOR = 20;
    private static final int LAT_FIRST_DIVISOR = 10;
    private static final int SECOND_DIVISOR = 2;
    private static final int GRID_CHAR_COUNT = 24;
    private static final int OFFSET_LON = 180;
    private static final int OFFSET_LAT = 90;

    /**
     * Pure function to calculate a 6-character locator.
     * @param lat Decimal Latitude
     * @param lon Decimal Longitude
     * @return 6-character Maidenhead locator (e.g., EN66gl)
     */
    public static String calculate(final double lat, final double lon) {
        final double adjLon = lon + OFFSET_LON;
        final double adjLat = lat + OFFSET_LAT;

        final char fLon = (char) ('A' + (int) (adjLon / LON_FIRST_DIVISOR));
        final char fLat = (char) ('A' + (int) (adjLat / LAT_FIRST_DIVISOR));

        final double lonGrid = (adjLon % LON_FIRST_DIVISOR) / SECOND_DIVISOR;
        final double latGrid = adjLat % LAT_FIRST_DIVISOR;

        final int sLon = (int) lonGrid;
        final int sLat = (int) latGrid;

        final char ssLon = (char) ('a' + (int) ((lonGrid - sLon) * GRID_CHAR_COUNT));
        final char ssLat = (char) ('a' + (int) ((latGrid - sLat) * GRID_CHAR_COUNT));

        return String.format("%c%c%d%d%c%c", fLon, fLat, sLon, sLat, ssLon, ssLat);
    }

    private GridSquareCalculator() {
        // Utility Class
    }
}
