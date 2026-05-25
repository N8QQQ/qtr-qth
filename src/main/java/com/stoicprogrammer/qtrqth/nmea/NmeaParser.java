package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Pure functional parser for NMEA 0183 sentences.
 * Talker-Agnostic: Supports $GP, $GN, $GL, $GA, etc.
 * Adheres to strict branchless mandates and finality requirements.
 */
public final class NmeaParser {
    private static final Logger logger = LoggerFactory.getLogger(NmeaParser.class);

    // Operational Constants
    private static final int MAX_SENTENCE_LENGTH = 128;
    private static final int MIN_SENTENCE_LENGTH = 10;
    private static final int MAX_FIELDS = 25;
    private static final int HEX_RADIX = 16;
    private static final int CHECKSUM_LENGTH = 2;
    private static final int NMEA_TIME_STRING_LEN = 6;
    private static final int HEADER_MIN_LEN = 6;
    private static final int TYPE_START_IDX = 3;
    private static final int TYPE_END_IDX = 6;
    private static final int SATS_PER_GSV_SENTENCE = 4;
    private static final int GSV_FIELD_STRIDE = 4;

    // Minimum Fields per Sentence Type
    private static final int MIN_FIELDS_GPRMC = 10;
    private static final int MIN_FIELDS_GPGGA = 10;
    private static final int MIN_FIELDS_GPZDA = 5;
    private static final int MIN_FIELDS_GPVTG = 9;
    private static final int MIN_FIELDS_GPGLL = 5;
    private static final int MIN_FIELDS_GPGSA = 15;
    private static final int MIN_FIELDS_GPGSV = 4;
    private static final int MIN_FIELDS_GPGST = 8;
    private static final int MIN_FIELDS_GPTXT = 4;
    private static final int MIN_FIELDS_GNGNS = 12;

    // Time Parsing Constants
    private static final int HOUR_START = 0;
    private static final int HOUR_END = 2;
    private static final int MINUTE_START = 2;
    private static final int MINUTE_END = 4;
    private static final int SECOND_START = 4;
    private static final int SECOND_END = 6;
    private static final double BILLION_NANOS = 1_000_000_000.0;
    private static final double DEGREES_DIVISOR = 100.0;
    private static final double MINUTES_DIVISOR = 60.0;

    // Functional Routing Table
    private final Map<String, BiFunction<String[], GpsData, GpsData>> parsers = Map.ofEntries(
        Map.entry("RMC", this::parseGprmc),
        Map.entry("GGA", this::parseGpgga),
        Map.entry("ZDA", this::parseGpzda),
        Map.entry("VTG", this::parseGpvtg),
        Map.entry("GLL", this::parseGpgll),
        Map.entry("GSA", this::parseGpgsa),
        Map.entry("GSV", this::parseGpgsv),
        Map.entry("GST", this::parseGpgst),
        Map.entry("GNS", this::parseGngns), 
        Map.entry("TXT", this::parseGptxt)
    );

    // NMEA Field Indices
    private static final int NMEA_TIME = 1;

    // RMC
    private static final int GPRMC_LAT = 3;
    private static final int GPRMC_LAT_DIR = 4;
    private static final int GPRMC_LON = 5;
    private static final int GPRMC_LON_DIR = 6;
    private static final int GPRMC_SPEED = 7;
    private static final int GPRMC_TRACK = 8;
    private static final int GPRMC_DATE = 9;

    // GGA
    private static final int GPGGA_LAT = 2;
    private static final int GPGGA_LAT_DIR = 3;
    private static final int GPGGA_LON = 4;
    private static final int GPGGA_LON_DIR = 5;
    private static final int GPGGA_SATS = 7;
    private static final int GPGGA_HDOP = 8;
    private static final int GPGGA_ALT = 9;

    // GNS (Similar to GGA but with 2-char mode at idx 6)
    private static final int GNGNS_LAT = 2;
    private static final int GNGNS_LAT_DIR = 3;
    private static final int GNGNS_LON = 4;
    private static final int GNGNS_LON_DIR = 5;
    private static final int GNGNS_SATS = 7;
    private static final int GNGNS_HDOP = 8;
    private static final int GNGNS_ALT = 9;

    // ZDA
    private static final int GPZDA_DAY = 2;
    private static final int GPZDA_MONTH = 3;
    private static final int GPZDA_YEAR = 4;

    // VTG
    private static final int GPVTG_TRACK = 1;
    private static final int GPVTG_SPEED_KNOTS = 5;

    // GLL
    private static final int GPGLL_LAT = 1;
    private static final int GPGLL_LAT_DIR = 2;
    private static final int GPGLL_LON = 3;
    private static final int GPGLL_LON_DIR = 4;
    private static final int GPGLL_TIME = 5;

    // GSA
    private static final int GPGSA_PDOP = 15;
    private static final int GPGSA_HDOP = 16;
    private static final int GPGSA_VDOP = 17;

    // GSV
    private static final int GSV_MSG_NUM = 2;
    private static final int GSV_SAT_START = 4;

    // GST
    private static final int GPGST_LAT_STD = 6;
    private static final int GPGST_LON_STD = 7;
    private static final int GPGST_ALT_STD = 8;

    // TXT
    private static final int GPTXT_MSG_START = 4;

    /**
     * Pure function to parse an NMEA sentence and merge it with previous state.
     */
    public GpsData parse(final String raw, final GpsData previous) {
        return Optional.ofNullable(raw)
            .map(s -> s.replaceAll("[^\\x20-\\x7E]", "").trim())
            .filter(s -> !s.isEmpty() && s.startsWith("$") && s.length() <= MAX_SENTENCE_LENGTH)
            .filter(s -> !s.contains("*") || isValidChecksum(s))
            .map(s -> s.split(",", MAX_FIELDS))
            .map(parts -> route(parts, previous))
            .orElse(previous);
    }

    /**
     * Extracts and normalizes the UTC timestamp from supported NMEA sentences.
     * @param raw The raw NMEA string.
     * @return An Optional containing a normalized 6-digit timestamp (HHMMSS).
     */
    public Optional<String> getTimestamp(final String raw) {
        return Optional.ofNullable(raw)
            .filter(s -> s.startsWith("$") && s.length() > MIN_SENTENCE_LENGTH)
            .map(s -> s.split(",", MAX_FIELDS))
            .flatMap(parts -> {
                final String type = getSentenceType(parts[0]);
                final int timeIdx = type.equals("GLL") ? GPGLL_TIME : NMEA_TIME;
                return parsers.containsKey(type) ? extractField(parts, timeIdx)
                        .filter(ts -> ts.length() >= NMEA_TIME_STRING_LEN)
                        .map(ts -> ts.substring(0, NMEA_TIME_STRING_LEN)) : Optional.<String>empty();
            });
    }

    /**
     * Checks if the sentence type is supported by the parser's routing table.
     * @param raw The raw NMEA string.
     * @return true if the sentence can be decoded.
     */
    public boolean isSupported(final String raw) {
        return Optional.ofNullable(raw)
            .map(s -> s.replaceAll("[^\\x20-\\x7E]", "").trim())
            .filter(s -> s.startsWith("$") && s.length() >= HEADER_MIN_LEN)
            .map(s -> s.split(",")[0])
            .map(this::getSentenceType)
            .map(parsers::containsKey)
            .orElse(false);
    }

    /**
     * Identifies if a sentence should trigger a telemetry pulse (Temporal Authority).
     * @param raw The raw NMEA string.
     * @return true if the sentence triggers a pulse.
     */
    public boolean isTrigger(final String raw) {
        return Optional.ofNullable(raw)
            .map(s -> s.replaceAll("[^\\x20-\\x7E]", "").trim())
            .filter(this::isSupported)
            .map(s -> s.split(",")[0])
            .map(this::getSentenceType)
            .map(type -> type.equals("ZDA") || type.equals("RMC"))
            .orElse(false);
    }

    private String getSentenceType(final String header) {
        return header.length() >= TYPE_END_IDX ? header.substring(TYPE_START_IDX, TYPE_END_IDX) : "";
    }

    private GpsData route(final String[] parts, final GpsData previous) {
        return Optional.ofNullable(parsers.get(getSentenceType(parts[0])))
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
                final LocalTime time = extractTime(p, NMEA_TIME).orElse(prev.utcTime());
                final double lat = extractCoordinate(p, GPRMC_LAT, GPRMC_LAT_DIR).orElse(prev.latitude());
                final double lon = extractCoordinate(p, GPRMC_LON, GPRMC_LON_DIR).orElse(prev.longitude());
                final LocalDate date = extractField(p, GPRMC_DATE)
                    .filter(s -> s.length() == NMEA_TIME_STRING_LEN)
                    .map(s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("ddMMyy")))
                    .orElse(prev.date());
                final double speed = extractField(p, GPRMC_SPEED).flatMap(Functional::tryParseDouble).orElse(prev.speedKnots());
                final double track = extractField(p, GPRMC_TRACK).flatMap(Functional::tryParseDouble).orElse(prev.trackTrue());

                return new GpsData(time, date, lat, lon, prev.altitude(), prev.satelliteCount(), speed, track, prev.hdop(), prev.vdop(), prev.pdop(), prev.latestDiagnostic(), prev.satellitesInView(), prev.precisionStats());
            }).orElse(prev);
    }

    private GpsData parseGpgga(final String[] parts, final GpsData prev) {
        return Optional.of(parts)
            .filter(p -> p.length >= MIN_FIELDS_GPGGA)
            .map(p -> {
                final LocalTime time = extractTime(p, NMEA_TIME).orElse(prev.utcTime());
                final double lat = extractCoordinate(p, GPGGA_LAT, GPGGA_LAT_DIR).orElse(prev.latitude());
                final double lon = extractCoordinate(p, GPGGA_LON, GPGGA_LON_DIR).orElse(prev.longitude());
                final int sats = extractField(p, GPGGA_SATS).flatMap(Functional::tryParseInt).orElse(prev.satelliteCount());
                final double alt = extractField(p, GPGGA_ALT).flatMap(Functional::tryParseDouble).orElse(prev.altitude());
                final double hdop = extractField(p, GPGGA_HDOP).flatMap(Functional::tryParseDouble).orElse(prev.hdop());

                return new GpsData(time, prev.date(), lat, lon, alt, sats, prev.speedKnots(), prev.trackTrue(), hdop, prev.vdop(), prev.pdop(), prev.latestDiagnostic(), prev.satellitesInView(), prev.precisionStats());
            }).orElse(prev);
    }

    private GpsData parseGngns(final String[] parts, final GpsData prev) {
        return Optional.of(parts)
            .filter(p -> p.length >= MIN_FIELDS_GNGNS)
            .map(p -> {
                final LocalTime time = extractTime(p, NMEA_TIME).orElse(prev.utcTime());
                final double lat = extractCoordinate(p, GNGNS_LAT, GNGNS_LAT_DIR).orElse(prev.latitude());
                final double lon = extractCoordinate(p, GNGNS_LON, GNGNS_LON_DIR).orElse(prev.longitude());
                final int sats = extractField(p, GNGNS_SATS).flatMap(Functional::tryParseInt).orElse(prev.satelliteCount());
                final double alt = extractField(p, GNGNS_ALT).flatMap(Functional::tryParseDouble).orElse(prev.altitude());
                final double hdop = extractField(p, GNGNS_HDOP).flatMap(Functional::tryParseDouble).orElse(prev.hdop());

                return new GpsData(time, prev.date(), lat, lon, alt, sats, prev.speedKnots(), prev.trackTrue(), hdop, prev.vdop(), prev.pdop(), prev.latestDiagnostic(), prev.satellitesInView(), prev.precisionStats());
            }).orElse(prev);
    }

    private GpsData parseGpzda(final String[] parts, final GpsData prev) {
        return Optional.of(parts)
            .filter(p -> p.length >= MIN_FIELDS_GPZDA)
            .map(p -> {
                final LocalTime time = extractTime(p, NMEA_TIME).orElse(prev.utcTime());
                final LocalDate date = extractField(p, GPZDA_DAY)
                    .flatMap(Functional::tryParseInt)
                    .flatMap(d -> extractField(p, GPZDA_MONTH)
                        .flatMap(Functional::tryParseInt)
                        .flatMap(m -> extractField(p, GPZDA_YEAR)
                            .flatMap(Functional::tryParseInt)
                            .map(y -> LocalDate.of(y, m, d))))
                    .orElse(prev.date());

                return new GpsData(time, date, prev.latitude(), prev.longitude(), prev.altitude(), prev.satelliteCount(), prev.speedKnots(), prev.trackTrue(), prev.hdop(), prev.vdop(), prev.pdop(), prev.latestDiagnostic(), prev.satellitesInView(), prev.precisionStats());
            }).orElse(prev);
    }

    private GpsData parseGpvtg(final String[] parts, final GpsData prev) {
        return Optional.of(parts)
            .filter(p -> p.length >= MIN_FIELDS_GPVTG)
            .map(p -> {
                final double track = extractField(p, GPVTG_TRACK).flatMap(Functional::tryParseDouble).orElse(prev.trackTrue());
                final double speed = extractField(p, GPVTG_SPEED_KNOTS).flatMap(Functional::tryParseDouble).orElse(prev.speedKnots());
                return new GpsData(prev.utcTime(), prev.date(), prev.latitude(), prev.longitude(), prev.altitude(), prev.satelliteCount(), speed, track, prev.hdop(), prev.vdop(), prev.pdop(), prev.latestDiagnostic(), prev.satellitesInView(), prev.precisionStats());
            }).orElse(prev);
    }

    private GpsData parseGpgll(final String[] parts, final GpsData prev) {
        return Optional.of(parts)
            .filter(p -> p.length >= MIN_FIELDS_GPGLL)
            .map(p -> {
                final double lat = extractCoordinate(p, GPGLL_LAT, GPGLL_LAT_DIR).orElse(prev.latitude());
                final double lon = extractCoordinate(p, GPGLL_LON, GPGLL_LON_DIR).orElse(prev.longitude());
                final LocalTime time = extractTime(p, GPGLL_TIME).orElse(prev.utcTime());
                return new GpsData(time, prev.date(), lat, lon, prev.altitude(), prev.satelliteCount(), prev.speedKnots(), prev.trackTrue(), prev.hdop(), prev.vdop(), prev.pdop(), prev.latestDiagnostic(), prev.satellitesInView(), prev.precisionStats());
            }).orElse(prev);
    }

    private GpsData parseGpgsa(final String[] parts, final GpsData prev) {
        return Optional.of(parts)
            .filter(p -> p.length >= MIN_FIELDS_GPGSA)
            .map(p -> {
                final double pdop = extractField(p, GPGSA_PDOP).flatMap(Functional::tryParseDouble).orElse(prev.pdop());
                final double hdop = extractField(p, GPGSA_HDOP).flatMap(Functional::tryParseDouble).orElse(prev.hdop());
                final double vdop = extractField(p, GPGSA_VDOP).flatMap(Functional::tryParseDouble).orElse(prev.vdop());
                return new GpsData(prev.utcTime(), prev.date(), prev.latitude(), prev.longitude(), prev.altitude(), prev.satelliteCount(), prev.speedKnots(), prev.trackTrue(), hdop, vdop, pdop, prev.latestDiagnostic(), prev.satellitesInView(), prev.precisionStats());
            }).orElse(prev);
    }

    private GpsData parseGpgsv(final String[] parts, final GpsData prev) {
        return Optional.of(parts)
            .filter(p -> p.length >= MIN_FIELDS_GPGSV)
            .map(p -> {
                final int msgNum = Functional.tryParseInt(p[GSV_MSG_NUM]).orElse(0);
                final List<GpsData.SatelliteFix> base = (msgNum == 1) ? List.of() : prev.satellitesInView();
                final List<GpsData.SatelliteFix> currentSats = Stream.concat(
                    base.stream(),
                    IntStream.iterate(GSV_SAT_START, i -> i + GSV_FIELD_STRIDE).limit(SATS_PER_GSV_SENTENCE)
                        .filter(i -> i + TYPE_START_IDX < p.length)
                        .mapToObj(i -> {
                            final int prn = Functional.tryParseInt(p[i]).orElse(0);
                            final int elev = Functional.tryParseInt(p[i + 1]).orElse(0);
                            final int azim = Functional.tryParseInt(p[i + 2]).orElse(0);
                            final int snr = Functional.tryParseInt(p[i + TYPE_START_IDX]).orElse(0);
                            return (prn > 0) ? Optional.of(new GpsData.SatelliteFix(prn, elev, azim, snr)) : Optional.<GpsData.SatelliteFix>empty();
                        })
                        .flatMap(Optional::stream)
                ).collect(Collectors.toUnmodifiableList());

                return new GpsData(prev.utcTime(), prev.date(), prev.latitude(), prev.longitude(), prev.altitude(), prev.satelliteCount(), prev.speedKnots(), prev.trackTrue(), prev.hdop(), prev.vdop(), prev.pdop(), prev.latestDiagnostic(), currentSats, prev.precisionStats());
            }).orElse(prev);
    }

    private GpsData parseGpgst(final String[] parts, final GpsData prev) {
        return Optional.of(parts)
            .filter(p -> p.length >= MIN_FIELDS_GPGST)
            .map(p -> {
                final double latDev = extractField(p, GPGST_LAT_STD).flatMap(Functional::tryParseDouble).orElse(0.0);
                final double lonDev = extractField(p, GPGST_LON_STD).flatMap(Functional::tryParseDouble).orElse(0.0);
                final double altDev = extractField(p, GPGST_ALT_STD).flatMap(Functional::tryParseDouble).orElse(0.0);
                return new GpsData(prev.utcTime(), prev.date(), prev.latitude(), prev.longitude(), prev.altitude(), prev.satelliteCount(), prev.speedKnots(), prev.trackTrue(), prev.hdop(), prev.vdop(), prev.pdop(), prev.latestDiagnostic(), prev.satellitesInView(), new GpsData.PrecisionStats(latDev, lonDev, altDev));
            }).orElse(prev);
    }

    private GpsData parseGptxt(final String[] parts, final GpsData prev) {
        final String msg = IntStream.range(GPTXT_MSG_START, parts.length)
            .mapToObj(i -> parts[i])
            .collect(Collectors.joining(","));
        final String cleanMsg = msg.contains("*") ? msg.substring(0, msg.lastIndexOf('*')) : msg;
        return new GpsData(prev.utcTime(), prev.date(), prev.latitude(), prev.longitude(), prev.altitude(), prev.satelliteCount(), prev.speedKnots(), prev.trackTrue(), prev.hdop(), prev.vdop(), prev.pdop(), cleanMsg, prev.satellitesInView(), prev.precisionStats());
    }

    private Optional<String> extractField(final String[] parts, final int index) {
        return index < parts.length ? Optional.ofNullable(parts[index]).filter(s -> !s.isEmpty()) : Optional.empty();
    }

    private Optional<LocalTime> extractTime(final String[] parts, final int index) {
        return extractField(parts, index)
            .filter(s -> s.length() >= NMEA_TIME_STRING_LEN)
            .flatMap(s -> Functional.tryParseInt(s.substring(HOUR_START, HOUR_END)).flatMap(hh -> 
                          Functional.tryParseInt(s.substring(MINUTE_START, MINUTE_END)).flatMap(mm -> 
                          Functional.tryParseInt(s.substring(SECOND_START, SECOND_END)).map(ss -> {
                              int ns = 0;
                              if (s.length() > SECOND_END && s.charAt(SECOND_END) == '.') {
                                  ns = Functional.tryParseDouble("0" + s.substring(SECOND_END))
                                           .map(frac -> (int) Math.round(frac * BILLION_NANOS))
                                           .orElse(0);
                              }
                              return LocalTime.of(hh, mm, ss, ns);
                          }))));
    }

    private Optional<Double> extractCoordinate(final String[] parts, final int coordIdx, final int dirIdx) {
        return extractField(parts, coordIdx)
            .flatMap(Functional::tryParseDouble)
            .flatMap(coord -> extractField(parts, dirIdx)
                .map(dir -> convertToDecimalDegrees(coord, dir)));
    }

    private double convertToDecimalDegrees(final double raw, final String direction) {
        final double degrees = raw / DEGREES_DIVISOR;
        final int degInt = (int) degrees;
        final double minutes = raw - (degInt * DEGREES_DIVISOR);
        final double decimal = degInt + (minutes / MINUTES_DIVISOR);
        return (direction.equals("S") || direction.equals("W")) ? -decimal : decimal;
    }
}
