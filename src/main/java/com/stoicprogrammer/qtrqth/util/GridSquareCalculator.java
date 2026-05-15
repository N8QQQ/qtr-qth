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
        // Adjust lon to 0-360 range
        final double lon_adj = lon + 180;
        final double lat_adj = lat + 90;

        // Field (Chars 1-2)
        final char field_lon = (char) ('A' + (int) (lon_adj / 20));
        final char field_lat = (char) ('A' + (int) (lat_adj / 10));

        // Square (Chars 3-4)
        final int square_lon = (int) ((lon_adj % 20) / 2);
        final int square_lat = (int) (lat_adj % 10);

        // Subsquare (Chars 5-6)
        final double sub_lon_rem = (lon_adj % 20) - (square_lon * 2);
        final double sub_lat_rem = lat_adj % 10 - square_lat;
        
        final char sub_lon = (char) ('a' + (int) (sub_lon_rem * 12));
        final char sub_lat = (char) ('a' + (int) (sub_lat_rem * 24));

        return String.valueOf(field_lon) + field_lat + square_lon + square_lat + sub_lon + sub_lat;
    }

    private GridSquareCalculator() {
        // Utility Class
    }
}
