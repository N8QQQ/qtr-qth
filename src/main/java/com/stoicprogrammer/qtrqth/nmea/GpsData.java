package com.stoicprogrammer.qtrqth.nmea;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    int satelliteCount,
    double speedKnots,
    double trackTrue,
    double hdop,
    double vdop,
    double pdop,
    String latestDiagnostic,
    List<SatelliteFix> satellitesInView,
    PrecisionStats precisionStats
) {
    private static final double DEFAULT_DOP = 99.9;

    /**
     * Legacy constructor for backward compatibility with existing tests.
     */
    public GpsData(final LocalTime utcTime, final LocalDate date, final double latitude, final double longitude, final double altitude, final int satelliteCount) {
        this(utcTime, date, latitude, longitude, altitude, satelliteCount, 0.0, 0.0, DEFAULT_DOP, DEFAULT_DOP, DEFAULT_DOP, "", List.of(), PrecisionStats.EMPTY);
    }

    /**
     * Uninitialized fix state for system bootstrapping.
     */
    public static final GpsData EMPTY = new GpsData(null, null, 0, 0, 0, 0, 0.0, 0.0, DEFAULT_DOP, DEFAULT_DOP, DEFAULT_DOP, "SYSTEM_READY", List.of(), PrecisionStats.EMPTY);

    /**
     * Consolidates signal intelligence metrics for the current view.
     */
    public SignalQuality signalQuality() {
        final double avgSnr = satellitesInView.stream()
            .mapToInt(SatelliteFix::snr)
            .filter(snr -> snr > 0)
            .average()
            .orElse(0.0);

        final long trackedSats = satellitesInView.stream()
            .filter(s -> s.snr() > 0)
            .count();

        return new SignalQuality(avgSnr, (int) trackedSats, satellitesInView.size());
    }

    @Override
    public String toString() {
        final String timeStr = Optional.ofNullable(utcTime)
            .map(t -> t.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
            .orElse("null");
            
        return String.format("UTC: %s | Date: %s | Lat: %.5f | Lon: %.5f | Alt: %.1fm | Sats: %d | HDOP: %.2f",
                timeStr, date, latitude, longitude, altitude, satelliteCount, hdop);
    }

    /**
     * Individual satellite metadata for SNR Matrix and Sky Map.
     */
    public record SatelliteFix(int prn, int elevation, int azimuth, int snr) {}

    /**
     * High-precision error statistics from $GPGST.
     */
    public record PrecisionStats(double latStdDev, double lonStdDev, double altStdDev) {
        public static final PrecisionStats EMPTY = new PrecisionStats(0, 0, 0);
    }

    /**
     * Consolidated Signal Intelligence record.
     */
    public record SignalQuality(double averageSnr, int trackedCount, int visibleCount) {
        public static final SignalQuality EMPTY = new SignalQuality(0.0, 0, 0);
        
        @Override
        public String toString() {
            return String.format("SNR: %.1f dB | Tracked: %d/%d", averageSnr, trackedCount, visibleCount);
        }
    }
}
