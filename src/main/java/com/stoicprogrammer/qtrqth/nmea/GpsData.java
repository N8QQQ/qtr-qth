package com.stoicprogrammer.qtrqth.nmea;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable record representing a GPS fix.
 */
public record GpsData(
    LocalTime utcTime,
    LocalDate date,
    double latitude,
    double longitude,
    double altitude,
    int satelliteCount
) {
    @Override
    public String toString() {
        String timeStr = (utcTime != null) ? utcTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "null";
        return String.format("UTC: %s | Date: %s | Lat: %.5f | Lon: %.5f | Alt: %.1fm | Sats: %d",
                timeStr, date, latitude, longitude, altitude, satelliteCount);
    }
}


