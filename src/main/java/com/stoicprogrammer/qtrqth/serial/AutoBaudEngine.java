package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
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
    private static final int SCAN_TIMEOUT_MS = 1500;
    private static final int BUFFER_SIZE = 1024;
    private static final int SLEEP_MS = 50;
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
            final long deadline = System.currentTimeMillis() + SCAN_TIMEOUT_MS;
            final StringBuilder lineBuilder = new StringBuilder();
            final NmeaParser validator = new NmeaParser();

            return Stream.generate(() -> System.currentTimeMillis() < deadline)
                .takeWhile(Boolean::booleanValue)
                .map(active -> {
                    if (port.bytesAvailable() > 0) {
                        final int read = port.readBytes(buffer, buffer.length);
                        return processChunk(buffer, read, lineBuilder, validator);
                    }
                    sleep(SLEEP_MS);
                    return false;
                })
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
        if (c == '$') {
            lineBuilder.setLength(0);
        }
        lineBuilder.append(c);
        if (c == '\n' || c == '\r') {
            final String sentence = lineBuilder.toString().trim();
            lineBuilder.setLength(0);
            return Optional.of(sentence.length() >= MIN_SENTENCE_LEN && sentence.startsWith("$") && validator.isSupported(sentence));
        }
        return Optional.empty();
    }

    private static void sleep(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
