package com.stoicprogrammer.qtrqth.util;

/**
 * Calculates Maidenhead Grid Squares from Latitude and Longitude.
 * Refactored for expression-based calculation and strict finality.
 */
public final class GridSquareCalculator {

    /**
     * Converts coordinates to a 6-character Grid Square.
     */
    public static String calculate(final double lat, final double lon) {
        // Normalize coordinates: 1 unit = 1 square (2 deg lon, 1 deg lat)
        final double lon_grid = (lon + 180.0) / 2.0;
        final double lat_grid = lat + 90.0;

        // Field (Chars 1-2): 18 fields of 10 units each
        final char f_lon = (char) ('A' + (int) (lon_grid / 10));
        final char f_lat = (char) ('A' + (int) (lat_grid / 10));

        // Square (Chars 3-4): 10 squares of 1 unit each
        final int s_lon = (int) (lon_grid % 10);
        final int s_lat = (int) (lat_grid % 10);

        // Subsquare (Chars 5-6): 24 subsquares of 1/24 unit each
        final char ss_lon = (char) ('a' + (int) ((lon_grid - (int) lon_grid) * 24));
        final char ss_lat = (char) ('a' + (int) ((lat_grid - (int) lat_grid) * 24));

        return String.format("%c%c%d%d%c%c", f_lon, f_lat, s_lon, s_lat, ss_lon, ss_lat);
    }

    private GridSquareCalculator() {
        // Utility Class
    }
}
