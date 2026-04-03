package com.stoicprogrammer.qtrqth.nmea;

import java.time.LocalDate;
import java.time.LocalTime;

public class GpsData {
    private final LocalTime utcTime;
    private final LocalDate date;
    private final double latitude;
    private final double longitude;
    private final double altitude;
    private final int satelliteCount;

    public GpsData(LocalTime utcTime, LocalDate date, double latitude, double longitude, double altitude, int satelliteCount) {
        this.utcTime = utcTime;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.satelliteCount = satelliteCount;
    }

    public LocalTime getUtcTime() { return utcTime; }
    public LocalDate getDate() { return date; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getAltitude() { return altitude; }
    public int getSatelliteCount() { return satelliteCount; }

    @Override
    public String toString() {
        String timeStr = (utcTime != null) ? utcTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) : "null";
        return String.format("UTC: %s | Date: %s | Lat: %.5f | Lon: %.5f | Alt: %.1fm | Sats: %d",
                timeStr, date, latitude, longitude, altitude, satelliteCount);
    }
}

