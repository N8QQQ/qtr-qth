package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator;
import com.stoicprogrammer.qtrqth.serial.PortDiscovery;
import com.stoicprogrammer.qtrqth.serial.SerialConnector;
import com.stoicprogrammer.qtrqth.util.GridSquareCalculator;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * qtr-qth: GPS Time & Location Sync for Amateur Radio.
 * 
 * Developed by Nicholas R. Ustick (N8QQQ)
 * StoicProgrammer.com
 * 
 * Copyright (c) 2026 Nicholas R. Ustick.
 * Licensed under the GNU General Public License v3.0.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  qtr-qth : GPS Time & Location Hub       ");
        System.out.println("==========================================");
        
        // 1. Load Configuration
        ConfigManager config = new ConfigManager("qtr-qth.properties");
        String ntpServer = config.getProperty("ntp.server");
        boolean simulationMode = Boolean.parseBoolean(config.getProperty("simulation.mode"));
        boolean showRaw = Boolean.parseBoolean(config.getProperty("display.raw.telemetry"));
        
        logger.info("Configuration Loaded - NTP: {}, SimMode: {}, RawTelemetry: {}", ntpServer, simulationMode, showRaw);

        // 2. Serial Discovery
        com.stoicprogrammer.qtrqth.serial.api.ISerialProvider provider;
        if (simulationMode) {
            provider = new com.stoicprogrammer.qtrqth.serial.simulation.SimulationSerialProvider();
        } else {
            provider = new com.stoicprogrammer.qtrqth.serial.jserialcomm.JSerialCommProvider();
        }
        
        PortDiscovery discovery = new PortDiscovery(provider, config);
        List<String> availablePorts = discovery.getAvailablePorts();
        logger.info("Scanning for serial devices... Found {} ports.", availablePorts.size());
        
        String likelyGps = discovery.findLikelyGpsPort();
        if (likelyGps != null) {
            logger.info("Likely GPS hardware identified on: {}", likelyGps);
        } else {
            logger.warn("No obvious GPS device detected by metadata scan.");
            if (!availablePorts.isEmpty()) {
                likelyGps = availablePorts.get(0);
                logger.info("Defaulting to first available port: {}", likelyGps);
            }
        }

        // 3. NTP Health Check
        try {
            NTPUDPClient client = new NTPUDPClient();
            client.setDefaultTimeout(5000);
            client.open();
            InetAddress hostAddr = InetAddress.getByName(ntpServer);
            TimeInfo info = client.getTime(hostAddr);
            long returnTime = info.getMessage().getTransmitTimeStamp().getTime();
            logger.info("NTP Network Time Status: OK ({})", Instant.ofEpochMilli(returnTime));
            client.close();
        } catch (Exception e) {
            logger.error("NTP Health Check failed: {}", e.getMessage());
        }

        // 4. Start Serial Ingestion
        if (likelyGps != null) {
            NmeaSentenceAccumulator accumulator = new NmeaSentenceAccumulator();
            NmeaParser parser = new NmeaParser();
            SerialConnector connector = new SerialConnector(config, accumulator, provider);
            
            // Functional State: Immutable record container
            AtomicReference<GpsData> currentFix = new AtomicReference<>(new GpsData(null, null, 0, 0, 0, 0));

            // Graceful Shutdown Hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutdown signal received. Closing resources...");
                connector.disconnect();
            }));

            logger.info("Connecting to {}...", likelyGps);
            
            // The Assembly Line: Functional Telemetry Pipeline
            connector.connect(likelyGps)
                .peek(sentence -> {
                    if (showRaw) logger.debug("[RAW] {}", sentence);
                })
                .map(sentence -> currentFix.updateAndGet(fix -> parser.parse(sentence, fix)))
                .filter(fix -> fix.utcTime() != null)
                .forEach(fix -> {
                    String grid = GridSquareCalculator.calculate(fix.latitude(), fix.longitude());
                    logger.info("GPS Fix Acquired: {} | Grid: {}", fix, grid);
                });

        } else {
            logger.error("System Failure: No serial ports available for GPS connection.");
        }
    }
}
