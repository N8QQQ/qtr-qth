package com.stoicprogrammer.qtrqth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public ConfigManager(final Path configPath) {
        // Load Defaults
        properties.setProperty("ntp.server", "pool.ntp.org,time.google.com,time.windows.com");
        properties.setProperty("serial.baud", "9600");
        properties.setProperty("sync.threshold.ms", "1000");
        properties.setProperty("gps.discovery.keywords", "gps,u-blox,prolific,silicon labs,gnss,receiver");
        properties.setProperty("display.raw.telemetry", "false");
        properties.setProperty("simulation.mode", "true");

        final java.io.File configFile = configPath.toFile();

        // Declarative Strategy Mapping
        Map.<Boolean, Consumer<java.io.File>>of(
            true, this::loadConfig,
            false, this::selfHeal
        ).get(configFile.exists()).accept(configFile);
    }

    /**
     * Evolves the raw properties into a high-fidelity, typed AppConfig record.
     * @return The immutable application configuration.
     */
    public AppConfig getConfig() {
        return new AppConfig(
            extractList("ntp.server", "pool.ntp.org"),
            extractInt("serial.baud", 9600),
            extractLong("sync.threshold.ms", 1000L),
            extractList("gps.discovery.keywords", "gps"),
            extractBoolean("display.raw.telemetry", false),
            extractBoolean("simulation.mode", true)
        );
    }

    private void loadConfig(final java.io.File file) {
        try (final FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
            logger.info("Configuration loaded from file: {}", file.getPath());
        } catch (final IOException e) {
            logger.error("Error loading properties file: {}. Using defaults.", e.getMessage());
        }
    }

    private void selfHeal(final java.io.File file) {
        try (final FileOutputStream fos = new FileOutputStream(file)) {
            properties.store(fos, "qtr-qth Configuration - StoicProgrammer.com");
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
            .flatMap(this::tryParseInt)
            .orElseGet(() -> {
                logger.warn("Property {} is missing or malformed. Using default: {}", key, defaultVal);
                return defaultVal;
            });
    }

    private long extractLong(final String key, final long defaultVal) {
        return getProperty(key)
            .flatMap(this::tryParseLong)
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

    private Optional<Integer> tryParseInt(final String s) {
        try { return Optional.of(Integer.parseInt(s)); } 
        catch (final NumberFormatException e) { return Optional.empty(); }
    }

    private Optional<Long> tryParseLong(final String s) {
        try { return Optional.of(Long.parseLong(s)); } 
        catch (final NumberFormatException e) { return Optional.empty(); }
    }
}
