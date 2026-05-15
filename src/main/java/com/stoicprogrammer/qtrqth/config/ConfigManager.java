package com.stoicprogrammer.qtrqth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Manages application configuration and defaults using purely functional dispatch.
 * All operations adhere to strict finality and immutability mandates.
 */
public final class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private final Properties properties = new Properties();

    public ConfigManager(final String configPath) {
        // Load Defaults
        properties.setProperty("ntp.server", "pool.ntp.org,time.google.com,time.windows.com");
        properties.setProperty("serial.baud", "9600");
        properties.setProperty("sync.threshold.ms", "1000");
        properties.setProperty("gps.discovery.keywords", "gps,u-blox,prolific,silicon labs,gnss,receiver");
        properties.setProperty("display.raw.telemetry", "false");
        properties.setProperty("simulation.mode", "true");

        final java.io.File configFile = new java.io.File(configPath);

        // Declarative Strategy Mapping
        Map.<Boolean, Consumer<java.io.File>>of(
            true, this::loadConfig,
            false, this::selfHeal
        ).get(configFile.exists()).accept(configFile);
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
}
