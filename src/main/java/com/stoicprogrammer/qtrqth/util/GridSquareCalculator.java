package com.stoicprogrammer.qtrqth.util;

/**
 * Calculates Maidenhead Grid Squares from Latitude and Longitude.
 */
public class GridSquareCalculator {

    /**
     * Converts coordinates to a 6-character Grid Square.
     */
    public static String calculate(double lat, double lon) {
        // Adjust lon to 0-360 range
        double lon_adj = lon + 180;
        double lat_adj = lat + 90;

        // Field (Chars 1-2)
        char field_lon = (char) ('A' + (int) (lon_adj / 20));
        char field_lat = (char) ('A' + (int) (lat_adj / 10));

        // Square (Chars 3-4)
        int square_lon = (int) ((lon_adj % 20) / 2);
        int square_lat = (int) (lat_adj % 10);

        // Subsquare (Chars 5-6)
        double sub_lon_rem = (lon_adj % 20) - (square_lon * 2);
        double sub_lat_rem = lat_adj % 10 - square_lat;
        
        char sub_lon = (char) ('a' + (int) (sub_lon_rem * 12));
        char sub_lat = (char) ('a' + (int) (sub_lat_rem * 24));

        return "" + field_lon + field_lat + square_lon + square_lat + sub_lon + sub_lat;
    }
}
