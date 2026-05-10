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
import org.slf4j.MDC;

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

            // The Assembly Line: Functional Telemetry Pipeline with MDC Tracking
            connector.connect(likelyGps)
                .map(TelemetryPulse::start)
                .peek(p -> { if (showRaw) p.logRaw(logger); })
                .map(p -> p.update(parser, currentFix))
                .filter(TelemetryPulse::hasValidFix)
                .forEach(p -> p.logFinal(logger));

        } else {
            logger.error("System Failure: No serial ports available for GPS connection.");
        }
    }

    /**
     * Contextual Wrapper for a single GPS Telemetry event.
     * Manages MDC Trace IDs and logging context.
     * Package-private for unit testing.
     */
    static record TelemetryPulse(String pulseId, String sentence, GpsData data) {
        
        static TelemetryPulse start(String sentence) {
            String id = String.format("%04X", (sentence.hashCode() & 0xFFFF));
            return new TelemetryPulse(id, sentence, null);
        }

        void logRaw(Logger log) {
            runWithContext(() -> log.debug("[RAW] {}", sentence));
        }

        TelemetryPulse update(NmeaParser parser, AtomicReference<GpsData> state) {
            GpsData next = state.updateAndGet(fix -> parser.parse(sentence, fix));
            return new TelemetryPulse(pulseId, sentence, next);
        }

        boolean hasValidFix() {
            return data != null && data.utcTime() != null;
        }

        void logFinal(Logger log) {
            runWithContext(() -> {
                String grid = GridSquareCalculator.calculate(data.latitude(), data.longitude());
                log.info("GPS Fix Acquired: {} | Grid: {}", data, grid);
            });
        }

        private void runWithContext(Runnable action) {
            MDC.put("pulseId", pulseId);
            try {
                action.run();
            } finally {
                MDC.clear();
            }
        }
    }
}
