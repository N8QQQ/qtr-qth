package com.stoicprogrammer.qtrqth.config;

import com.stoicprogrammer.qtrqth.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Manages application configuration using purely functional dispatch.
 * Produces an immutable AppConfig record to ensure typed safety.
 */
public final class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private final Properties properties = new Properties();

    // Default Operational Constants
    private static final int DEFAULT_BAUD = 9600;
    private static final long DEFAULT_SYNC_THRESHOLD = 1000L;

    /**
     * Internal interface for mocking file operations.
     */
    @FunctionalInterface
    public interface FileAction {
        void run(File file, Properties props) throws IOException;
    }

    private final FileAction loader;
    private final FileAction saver;

    public ConfigManager(final Path configPath) {
        this(configPath, 
             (f, p) -> { try (var is = new FileInputStream(f)) { p.load(is); } },
             (f, p) -> { try (var os = new FileOutputStream(f)) { p.store(os, "qtr-qth"); } });
    }

    /**
     * Injection constructor for high-fidelity testing of environmental failures.
     */
    public ConfigManager(final Path configPath, final FileAction loader, final FileAction saver) {
        this.loader = loader;
        this.saver = saver;

        // Load Defaults
        properties.setProperty("ntp.server", "pool.ntp.org,time.google.com,time.windows.com");
        properties.setProperty("serial.baud", String.valueOf(DEFAULT_BAUD));
        properties.setProperty("sync.threshold.ms", String.valueOf(DEFAULT_SYNC_THRESHOLD));
        properties.setProperty("gps.discovery.keywords", "gps,u-blox,prolific,silicon labs,gnss,receiver,ttyusb");
        properties.setProperty("display.raw.telemetry", "false");
        properties.setProperty("simulation.mode", "false");

        final File configFile = configPath.toFile();

        // Declarative Strategy Mapping
        Map.<Boolean, Consumer<File>>of(
            true, this::loadConfig,
            false, this::selfHeal
        ).get(configFile.exists()).accept(configFile);
    }

    public AppConfig getConfig() {
        return new AppConfig(
            extractList("ntp.server", "pool.ntp.org"),
            extractInt("serial.baud", DEFAULT_BAUD),
            extractLong("sync.threshold.ms", DEFAULT_SYNC_THRESHOLD),
            extractList("gps.discovery.keywords", "gps"),
            extractBoolean("display.raw.telemetry", false),
            extractBoolean("simulation.mode", true)
        );
    }

    private void loadConfig(final File file) {
        try {
            loader.run(file, properties);
            logger.info("Configuration loaded from file: {}", file.getPath());
        } catch (final IOException e) {
            logger.error("Error loading properties file: {}. Using defaults.", e.getMessage());
        }
    }

    private void selfHeal(final File file) {
        try {
            saver.run(file, properties);
            logger.info("Default configuration file created at: {}", file.getPath());
        } catch (final IOException e) {
            logger.warn("Could not save default properties to {}: {}", file.getPath(), e.getMessage());
        }
    }

    public Optional<String> getProperty(final String key) {
        return Optional.ofNullable(properties.getProperty(key));
    }

    private List<String> extractList(final String key, final String defaultVal) {
        return java.util.Arrays.stream(getProperty(key).orElse(defaultVal).split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    private int extractInt(final String key, final int defaultVal) {
        return getProperty(key)
            .flatMap(Functional::tryParseInt)
            .orElseGet(() -> {
                logger.warn("Property {} is missing or malformed. Using default: {}", key, defaultVal);
                return defaultVal;
            });
    }

    private long extractLong(final String key, final long defaultVal) {
        return getProperty(key)
            .flatMap(Functional::tryParseLong)
            .orElseGet(() -> {
                logger.warn("Property {} is missing or malformed. Using default: {}", key, defaultVal);
                return defaultVal;
            });
    }

    private boolean extractBoolean(final String key, final boolean defaultVal) {
        return getProperty(key)
            .map(Boolean::parseBoolean)
            .orElse(defaultVal);
    }
}
