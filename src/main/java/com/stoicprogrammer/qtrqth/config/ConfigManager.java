package com.stoicprogrammer.qtrqth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Manages application configuration and defaults.
 */
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private final Properties properties = new Properties();

    public ConfigManager(String configPath) {
        // Load Defaults
        properties.setProperty("ntp.server", "pool.ntp.org");
        properties.setProperty("serial.baud", "9600");
        properties.setProperty("sync.threshold.ms", "1000");
        properties.setProperty("gps.discovery.keywords", "gps,u-blox,prolific,silicon labs,gnss,receiver");
        properties.setProperty("display.raw.telemetry", "false");
        properties.setProperty("simulation.mode", "true");

        java.io.File configFile = new java.io.File(configPath);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                properties.load(fis);
                logger.info("Configuration loaded from file: {}", configPath);
            } catch (IOException e) {
                logger.error("Error loading properties file: {}. Using defaults.", e.getMessage());
            }
        } else {
            // Self-heal: Create the file with defaults
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(configFile)) {
                properties.store(fos, "qtr-qth Configuration - StoicProgrammer.com");
                logger.info("Default configuration file created at: {}", configPath);
            } catch (IOException e) {
                logger.warn("Could not save default properties to {}: {}", configPath, e.getMessage());
            }
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
