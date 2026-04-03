package com.stoicprogrammer.qtrqth.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {
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
            } catch (IOException e) {
                System.out.println("[CONFIG] Error loading properties: " + e.getMessage());
            }
        } else {
            // Self-heal: Create the file with defaults
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(configFile)) {
                properties.store(fos, "qtr-qth Configuration - StoicProgrammer.com");
                System.out.println("[CONFIG] Created default configuration: " + configPath);
            } catch (IOException e) {
                System.out.println("[CONFIG] Could not save default properties: " + e.getMessage());
            }
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
