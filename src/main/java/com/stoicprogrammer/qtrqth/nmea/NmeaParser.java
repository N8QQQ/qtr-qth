package com.stoicprogrammer.qtrqth.nmea;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class NmeaParser {

    private LocalTime lastTime;
    private LocalDate lastDate;
    private double lastLat = 0.0;
    private double lastLon = 0.0;
    private double lastAlt = 0.0;
    private int lastSats = 0;

    public GpsData parse(String sentence) {
        if (sentence == null || !sentence.startsWith("$")) return null;

        // Verify Checksum if present
        if (sentence.contains("*")) {
            if (!isValidChecksum(sentence)) {
                System.err.println("[PARSER] Warning: Invalid checksum for sentence: " + sentence);
                return null;
            }
        }

        String[] parts = sentence.split(",", -1); 
        String type = parts[0];

        if (type.equals("$GPRMC")) {
            parseGprmc(parts);
        } else if (type.equals("$GPGGA")) {
            parseGpgga(parts);
        } else if (type.equals("$GPZDA")) {
            parseGpzda(parts);
        }

        return new GpsData(lastTime, lastDate, lastLat, lastLon, lastAlt, lastSats);
    }

    private boolean isValidChecksum(String sentence) {
        int starIndex = sentence.indexOf('*');
        if (starIndex == -1 || starIndex + 3 > sentence.length()) return false;

        String content = sentence.substring(1, starIndex);
        String hexSum = sentence.substring(starIndex + 1, starIndex + 3);

        int checksum = 0;
        for (char c : content.toCharArray()) {
            checksum ^= c;
        }

        try {
            int expected = Integer.parseInt(hexSum, 16);
            return checksum == expected;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void parseGprmc(String[] parts) {
        if (parts.length < 10) return;

        if (parts[1] != null && parts[1].length() >= 6) {
            lastTime = LocalTime.parse(parts[1].substring(0, 6), DateTimeFormatter.ofPattern("HHmmss"));
        }

        if (!parts[3].isEmpty() && !parts[4].isEmpty()) {
            lastLat = convertToDecimalDegrees(parts[3], parts[4]);
        }

        if (!parts[5].isEmpty() && !parts[6].isEmpty()) {
            lastLon = convertToDecimalDegrees(parts[5], parts[6]);
        }
        
        if (!parts[9].isEmpty()) {
            lastDate = LocalDate.parse(parts[9], DateTimeFormatter.ofPattern("ddMMyy"));
        }
    }

    private void parseGpgga(String[] parts) {
        if (parts.length < 10) return;

        if (!parts[7].isEmpty()) {
            lastSats = Integer.parseInt(parts[7]);
        }

        if (!parts[9].isEmpty()) {
            lastAlt = Double.parseDouble(parts[9]);
        }
    }

    private void parseGpzda(String[] parts) {
        if (parts.length < 5) return;

        if (!parts[2].isEmpty() && !parts[3].isEmpty() && !parts[4].isEmpty()) {
            int day = Integer.parseInt(parts[2]);
            int month = Integer.parseInt(parts[3]);
            int year = Integer.parseInt(parts[4]);
            lastDate = LocalDate.of(year, month, day);
        }
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

