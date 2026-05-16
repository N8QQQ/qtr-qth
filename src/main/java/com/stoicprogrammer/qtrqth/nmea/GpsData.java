package com.stoicprogrammer.qtrqth.nmea;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Immutable record representing a GPS fix.
 * Adheres to strict finality and functional scannability.
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
        final String timeStr = Optional.ofNullable(utcTime)
            .map(t -> t.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
            .orElse("null");
            
        return String.format("UTC: %s | Date: %s | Lat: %.5f | Lon: %.5f | Alt: %.1fm | Sats: %d",
                timeStr, date, latitude, longitude, altitude, satelliteCount);
    }
}
