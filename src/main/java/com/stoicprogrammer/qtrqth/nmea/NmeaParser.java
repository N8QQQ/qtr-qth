package com.stoicprogrammer.qtrqth.nmea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Pure functional parser for NMEA 0183 sentences.
 */
public class NmeaParser {
    private static final Logger logger = LoggerFactory.getLogger(NmeaParser.class);

    // NMEA Field Indices
    private static final int GPRMC_TIME = 1;
    private static final int GPRMC_LAT = 3;
    private static final int GPRMC_LAT_DIR = 4;
    private static final int GPRMC_LON = 5;
    private static final int GPRMC_LON_DIR = 6;
    private static final int GPRMC_DATE = 9;

    private static final int GPGGA_SATS = 7;
    private static final int GPGGA_ALT = 9;

    private static final int GPZDA_DAY = 2;
    private static final int GPZDA_MONTH = 3;
    private static final int GPZDA_YEAR = 4;

    /**
     * Pure function to parse an NMEA sentence and merge it with previous state.
     * @param sentence The raw NMEA string.
     * @param previous The previous GpsData state.
     * @return A new GpsData record with updated fields.
     */
    public GpsData parse(String sentence, GpsData previous) {
        if (sentence == null || !sentence.startsWith("$")) return previous;

        // Verify Checksum if present
        if (sentence.contains("*")) {
            if (!isValidChecksum(sentence)) {
                logger.warn("Invalid NMEA checksum detected. Sentence ignored: {}", sentence);
                return previous;
            }
        }

        String[] parts = sentence.split(",", -1); 
        String type = parts[0];

        return switch (type) {
            case "$GPRMC" -> parseGprmc(parts, previous);
            case "$GPGGA" -> parseGpgga(parts, previous);
            case "$GPZDA" -> parseGpzda(parts, previous);
            default -> previous;
        };
    }

    private boolean isValidChecksum(String sentence) {
        int starIndex = sentence.lastIndexOf('*');
        if (starIndex == -1 || starIndex + 1 >= sentence.length()) return false;

        // Content is between $ and *
        String content = sentence.substring(1, starIndex);
        String hexSum = sentence.substring(starIndex + 1).trim();
        
        // Only take the first 2 chars of the hex sum (ignore trailing CRLF)
        if (hexSum.length() > 2) hexSum = hexSum.substring(0, 2);

        int calculated = 0;
        for (char c : content.toCharArray()) {
            calculated ^= c;
            if (logger.isTraceEnabled()) {
                logger.trace(String.format("XOR Char: '%s' (0x%02X) -> Running Sum: 0x%02X", c, (int)c, calculated));
            }
        }

        try {
            int expected = Integer.parseInt(hexSum, 16);
            if (calculated != expected) {
                logger.debug("Checksum Mismatch: Calculated {:02X}, Expected {:02X} for: {}", calculated, expected, content);
            }
            return calculated == expected;
        } catch (NumberFormatException e) {
            logger.debug("Checksum Format Error: '{}' is not valid hex", hexSum);
            return false;
        }
    }

    private GpsData parseGprmc(String[] parts, GpsData prev) {
        if (parts.length < 10) return prev;

        LocalTime time = prev.utcTime();
        if (parts[GPRMC_TIME] != null && parts[GPRMC_TIME].length() >= 6) {
            time = LocalTime.parse(parts[GPRMC_TIME].substring(0, 6), DateTimeFormatter.ofPattern("HHmmss"));
        }

        double lat = prev.latitude();
        if (!parts[GPRMC_LAT].isEmpty() && !parts[GPRMC_LAT_DIR].isEmpty()) {
            lat = convertToDecimalDegrees(parts[GPRMC_LAT], parts[GPRMC_LAT_DIR]);
        }

        double lon = prev.longitude();
        if (!parts[GPRMC_LON].isEmpty() && !parts[GPRMC_LON_DIR].isEmpty()) {
            lon = convertToDecimalDegrees(parts[GPRMC_LON], parts[GPRMC_LON_DIR]);
        }
        
        LocalDate date = prev.date();
        if (!parts[GPRMC_DATE].isEmpty()) {
            date = LocalDate.parse(parts[GPRMC_DATE], DateTimeFormatter.ofPattern("ddMMyy"));
        }

        return new GpsData(time, date, lat, lon, prev.altitude(), prev.satelliteCount());
    }

    private GpsData parseGpgga(String[] parts, GpsData prev) {
        if (parts.length < 10) return prev;

        int sats = prev.satelliteCount();
        if (!parts[GPGGA_SATS].isEmpty()) {
            sats = Integer.parseInt(parts[GPGGA_SATS]);
        }

        double alt = prev.altitude();
        if (!parts[GPGGA_ALT].isEmpty()) {
            alt = Double.parseDouble(parts[GPGGA_ALT]);
        }

        return new GpsData(prev.utcTime(), prev.date(), prev.latitude(), prev.longitude(), alt, sats);
    }

    private GpsData parseGpzda(String[] parts, GpsData prev) {
        if (parts.length < 5) return prev;

        LocalDate date = prev.date();
        if (!parts[GPZDA_DAY].isEmpty() && !parts[GPZDA_MONTH].isEmpty() && !parts[GPZDA_YEAR].isEmpty()) {
            int day = Integer.parseInt(parts[GPZDA_DAY]);
            int month = Integer.parseInt(parts[GPZDA_MONTH]);
            int year = Integer.parseInt(parts[GPZDA_YEAR]);
            date = LocalDate.of(year, month, day);
        }

        return new GpsData(prev.utcTime(), date, prev.latitude(), prev.longitude(), prev.altitude(), prev.satelliteCount());
    }

    private double convertToDecimalDegrees(String nmeaCoord, String direction) {
        double raw = Double.parseDouble(nmeaCoord);
        int degrees = (int) (raw / 100);
        double minutes = raw - (degrees * 100);
        double decimal = degrees + (minutes / 60);
        
        if (direction.equals("S") || direction.equals("W")) {
            decimal *= -1;
        }
        return decimal;
    }
}
