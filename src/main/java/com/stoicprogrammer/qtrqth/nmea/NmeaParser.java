package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Pure functional parser for NMEA 0183 sentences.
 * Adheres to strict branchless mandates and finality requirements.
 */
public final class NmeaParser {
    private static final Logger logger = LoggerFactory.getLogger(NmeaParser.class);

    // Operational Constants
    private static final int MAX_SENTENCE_LENGTH = 128;
    private static final int MIN_FIELDS_GPRMC = 10;
    private static final int MIN_FIELDS_GPGGA = 10;
    private static final int MIN_FIELDS_GPZDA = 5;
    private static final int HEX_RADIX = 16;
    private static final int CHECKSUM_LENGTH = 2;

    // Functional Routing Table: Logic treated as Data
    private final Map<String, BiFunction<String[], GpsData, GpsData>> parsers = Map.of(
        "$GPRMC", this::parseGprmc,
        "$GPGGA", this::parseGpgga,
        "$GPZDA", this::parseGpzda
    );

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
     * @param raw The raw NMEA string from the serial port.
     * @param previous The previous GpsData state.
     * @return A new GpsData record with updated fields.
     */
    public GpsData parse(final String raw, final GpsData previous) {
        return Optional.ofNullable(raw)
            .map(s -> s.replaceAll("[^\\x20-\\x7E]", "").trim())
            .filter(s -> !s.isEmpty() && s.startsWith("$") && s.length() <= MAX_SENTENCE_LENGTH)
            .filter(s -> !s.contains("*") || isValidChecksum(s))
            .map(s -> s.split(",", 20))
            .map(parts -> route(parts, previous))
            .orElse(previous);
    }

    private GpsData route(final String[] parts, final GpsData previous) {
        return Optional.ofNullable(parsers.get(parts[0]))
            .map(parser -> parser.apply(parts, previous))
            .orElse(previous);
    }

    private boolean isValidChecksum(final String sentence) {
        return Optional.of(sentence.lastIndexOf('*'))
            .filter(idx -> idx != -1 && idx + 1 < sentence.length())
            .map(idx -> {
                final String content = sentence.substring(1, idx);
                final String rawHexSum = sentence.substring(idx + 1).trim();
                final String hexSum = Optional.of(rawHexSum)
                    .filter(s -> s.length() > CHECKSUM_LENGTH)
                    .map(s -> s.substring(0, CHECKSUM_LENGTH))
                    .orElse(rawHexSum);
                
                final int calculated = content.chars().reduce(0, (a, b) -> a ^ b);
                return Functional.tryParseInt(hexSum, HEX_RADIX)
                    .filter(expected -> calculated == expected)
                    .isPresent();
            })
            .orElse(false);
    }

    private GpsData parseGprmc(final String[] parts, final GpsData prev) {
        return Optional.of(parts)
            .filter(p -> p.length >= MIN_FIELDS_GPRMC)
            .map(p -> {
                final LocalTime time = extractField(p, GPRMC_TIME)
                    .filter(s -> s.length() >= 6)
                    .map(s -> LocalTime.parse(s.substring(0, 6), DateTimeFormatter.ofPattern("HHmmss")))
                    .orElse(prev.utcTime());

                final double lat = extractCoordinate(p, GPRMC_LAT, GPRMC_LAT_DIR).orElse(prev.latitude());
                final double lon = extractCoordinate(p, GPRMC_LON, GPRMC_LON_DIR).orElse(prev.longitude());

                final LocalDate date = extractField(p, GPRMC_DATE)
                    .map(s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("ddMMyy")))
                    .orElse(prev.date());

                return new GpsData(time, date, lat, lon, prev.altitude(), prev.satelliteCount());
            })
            .orElse(prev);
    }

    private GpsData parseGpgga(final String[] parts, final GpsData prev) {
        return Optional.of(parts)
            .filter(p -> p.length >= MIN_FIELDS_GPGGA)
            .map(p -> {
                final int sats = extractField(p, GPGGA_SATS)
                    .flatMap(Functional::tryParseInt)
                    .orElse(prev.satelliteCount());

                final double alt = extractField(p, GPGGA_ALT)
                    .flatMap(Functional::tryParseDouble)
                    .orElse(prev.altitude());

                return new GpsData(prev.utcTime(), prev.date(), prev.latitude(), prev.longitude(), alt, sats);
            })
            .orElse(prev);
    }

    private GpsData parseGpzda(final String[] parts, final GpsData prev) {
        return Optional.of(parts)
            .filter(p -> p.length >= MIN_FIELDS_GPZDA)
            .map(p -> {
                final LocalDate date = extractField(p, GPZDA_DAY)
                    .flatMap(Functional::tryParseInt)
                    .flatMap(d -> extractField(p, GPZDA_MONTH)
                        .flatMap(Functional::tryParseInt)
                        .flatMap(m -> extractField(p, GPZDA_YEAR)
                            .flatMap(Functional::tryParseInt)
                            .map(y -> LocalDate.of(y, m, d))))
                    .orElse(prev.date());

                return new GpsData(prev.utcTime(), date, prev.latitude(), prev.longitude(), prev.altitude(), prev.satelliteCount());
            })
            .orElse(prev);
    }

    private Optional<String> extractField(final String[] parts, final int index) {
        return Optional.ofNullable(parts[index]).filter(s -> !s.isEmpty());
    }

    private Optional<Double> extractCoordinate(final String[] parts, final int coordIdx, final int dirIdx) {
        return extractField(parts, coordIdx)
            .flatMap(Functional::tryParseDouble)
            .flatMap(coord -> extractField(parts, dirIdx)
                .map(dir -> convertToDecimalDegrees(coord, dir)));
    }

    private double convertToDecimalDegrees(final double raw, final String direction) {
        final double degrees = raw / 100.0;
        final int degInt = (int) degrees;
        final double minutes = raw - (degInt * 100.0);
        final double decimal = degInt + (minutes / 60.0);
        return (direction.equals("S") || direction.equals("W")) ? -decimal : decimal;
    }
}
