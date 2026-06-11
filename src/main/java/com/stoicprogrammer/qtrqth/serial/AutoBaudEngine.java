package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Intelligent engine for detecting the baud rate of serial GNSS hardware.
 * Uses NMEA checksum validation to certify successful synchronization.
 */
public final class AutoBaudEngine {
    private static final Logger logger = LoggerFactory.getLogger(AutoBaudEngine.class);

    // Operational Constants
    private static final List<Integer> BAUD_RATES = List.of(115200, 9600, 38400, 230400, 460800, 4800);
    private static final int SCAN_TIMEOUT_MILLISECONDS = 1500;
    private static final int BUFFER_SIZE = 1024;
    private static final int SLEEP_MILLISECONDS = 50;
    private static final int MIN_SENTENCE_LEN = 6;

    private AutoBaudEngine() {
        // Utility Class
    }

    /**
     * Scans the specified port across common frequencies to identify the active baud rate.
     * @param port The serial port to audit.
     * @return An Optional containing the detected baud rate, or empty if no GNSS signal found.
     */
    public static Optional<Integer> scan(final ISerialPort port) {
        return BAUD_RATES.stream()
            .filter(baud -> attemptBaud(port, baud))
            .findFirst();
    }

    private static boolean attemptBaud(final ISerialPort port, final int baud) {
        logger.debug("Auto-Baud: Auditing {} at {} bps...", port.getSystemPortName(), baud);
        port.closePort(); 
        port.setBaudRate(baud);
        
        return Optional.of(port.openPort())
            .filter(Boolean::booleanValue)
            .map(opened -> performReadAudit(port))
            .orElse(false);
    }

    private static boolean performReadAudit(final ISerialPort port) {
        try {
            final byte[] buffer = new byte[BUFFER_SIZE];
            final long deadline = System.currentTimeMillis() + SCAN_TIMEOUT_MILLISECONDS;
            final StringBuilder lineBuilder = new StringBuilder();
            final NmeaParser validator = new NmeaParser();

            return Stream.generate(() -> System.currentTimeMillis() < deadline)
                .takeWhile(Boolean::booleanValue)
                .map(active -> Map.<Boolean, Supplier<Boolean>>of(
                    true, () -> processChunk(buffer, port.readBytes(buffer, buffer.length), lineBuilder, validator),
                    false, () -> {
                        sleep(SLEEP_MILLISECONDS);
                        return false;
                    }
                ).get(port.bytesAvailable() > 0).get())
                .anyMatch(Boolean::booleanValue);
        } finally {
            port.closePort();
        }
    }

    private static boolean processChunk(final byte[] buffer, final int read, final StringBuilder lineBuilder, final NmeaParser validator) {
        return IntStream.range(0, read)
            .mapToObj(i -> (char) buffer[i])
            .map(c -> processChar(c, lineBuilder, validator))
            .flatMap(Optional::stream)
            .anyMatch(Boolean::booleanValue);
    }

    private static Optional<Boolean> processChar(final char c, final StringBuilder lineBuilder, final NmeaParser validator) {
        // Pure Functional Reset Logic
        Map.<Boolean, Runnable>of(
            true, () -> lineBuilder.setLength(0),
            false, () -> {}
        ).get(c == '$').run();

        lineBuilder.append(c);

        return Optional.of(c == '\n' || c == '\r')
            .filter(Boolean::booleanValue)
            .map(eol -> {
                final String sentence = lineBuilder.toString().trim();
                lineBuilder.setLength(0);
                return sentence.length() >= MIN_SENTENCE_LEN && sentence.startsWith("$") && validator.isSupported(sentence);
            });
    }

    private static void sleep(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
