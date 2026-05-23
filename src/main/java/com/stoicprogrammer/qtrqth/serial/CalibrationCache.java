package com.stoicprogrammer.qtrqth.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/**
 * Persistence layer for Device Calibration Data.
 * Allows the system to bypass the calibration phase for known hardware.
 */
public final class CalibrationCache {
    private static final Logger logger = LoggerFactory.getLogger(CalibrationCache.class);
    private static final String CACHE_DIR = "config/calibration";
    private static final String SENTINEL_KEY = "sentinel";

    private CalibrationCache() {
        // Prevent instantiation of utility class
    }

    /**
     * Attempts to load the sentinel for a specific port.
     * @param portName The system port name (e.g., COM3, /dev/ttyUSB0).
     * @return An Optional containing the cached sentinel.
     */
    public static Optional<String> load(final String portName) {
        final Path cachePath = getPath(portName);
        if (!Files.exists(cachePath)) {
            return Optional.empty();
        }

        final Properties props = new Properties();
        try (var is = new FileInputStream(cachePath.toFile())) {
            props.load(is);
            return Optional.ofNullable(props.getProperty(SENTINEL_KEY));
        } catch (final IOException e) {
            logger.warn("Failed to load calibration cache for {}: {}", portName, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Saves the identified sentinel to the persistent cache.
     * @param portName The system port name.
     * @param sentinel The identified sentinel sentence type.
     * @param clock The time source for metadata.
     */
    public static void save(final String portName, final String sentinel, final java.time.InstantSource clock) {
        final Path cachePath = getPath(portName);
        try {
            Files.createDirectories(cachePath.getParent());
            final Properties props = new Properties();
            props.setProperty(SENTINEL_KEY, sentinel);
            props.setProperty("timestamp", clock.instant().toString());
            
            saveProperties(cachePath, props, portName);
        } catch (final IOException e) {
            logger.warn("Failed to save calibration cache for {}: {}", portName, e.getMessage());
        }
    }

    private static void saveProperties(final Path path, final Properties props, final String portName) throws IOException {
        try (var os = new FileOutputStream(path.toFile())) {
            props.store(os, "qtr-qth Device Calibration Data");
            logger.info("CALIBRATION SAVED: Profile cached for port {}.", portName);
        }
    }

    private static Path getPath(final String portName) {
        // Sanitize port name for filename safety
        final String safeName = portName.replaceAll("[^a-zA-Z0-9]", "_");
        return Path.of(CACHE_DIR, safeName + ".properties");
    }
}
