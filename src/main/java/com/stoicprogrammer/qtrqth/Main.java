package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator;
import com.stoicprogrammer.qtrqth.ntp.NtpClient;
import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import com.stoicprogrammer.qtrqth.serial.PortDiscovery;
import com.stoicprogrammer.qtrqth.serial.SerialConnector;
import com.stoicprogrammer.qtrqth.util.GridSquareCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * qtr-qth: GPS Time & Location Sync for Amateur Radio.
 */
public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private Main() {
        // Utility Class
    }

    public static void main(final String[] args) {
        System.out.println("==========================================");
        System.out.println("  qtr-qth : GPS Time & Location Hub       ");
        System.out.println("==========================================");
        
        final ConfigManager config = new ConfigManager("qtr-qth.properties");
        final List<String> ntpPool = config.getProperty("ntp.server")
            .map(s -> java.util.Arrays.stream(s.split(",")).toList())
            .orElse(List.of("pool.ntp.org"));
        
        final boolean simulationMode = config.getProperty("simulation.mode")
            .map(Boolean::parseBoolean)
            .orElse(true);
            
        final boolean showRaw = config.getProperty("display.raw.telemetry")
            .map(Boolean::parseBoolean)
            .orElse(false);
        
        logger.info("Configuration Loaded - NTP Pool: {}, SimMode: {}, RawTelemetry: {}", ntpPool, simulationMode, showRaw);

        final com.stoicprogrammer.qtrqth.serial.api.ISerialProvider provider = simulationMode 
            ? new com.stoicprogrammer.qtrqth.serial.simulation.SimulationSerialProvider()
            : new com.stoicprogrammer.qtrqth.serial.jserialcomm.JSerialCommProvider();
        
        final PortDiscovery discovery = new PortDiscovery(provider, config);
        final List<String> availablePorts = discovery.getAvailablePorts();
        logger.info("Scanning for serial devices... Found {} ports.", availablePorts.size());
        
        discovery.findLikelyGpsPort()
            .or(() -> {
                logger.warn("No obvious GPS device detected by metadata scan.");
                return availablePorts.stream().findFirst();
            })
            .ifPresentOrElse(
                port -> startTelemetryPipeline(port, config, provider, ntpPool, showRaw), 
                () -> logger.error("No serial ports available for GPS connection. System operational failure.")
            );
    }

    private static void startTelemetryPipeline(final String port, final ConfigManager config, final com.stoicprogrammer.qtrqth.serial.api.ISerialProvider provider, final List<String> ntpPool, final boolean showRaw) {
        final NtpClient ntpClient = new NtpClient(5000);
        final AtomicReference<NtpResponse> lastNtp = new AtomicReference<>();
        
        final ScheduledExecutorService ntpExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "ntp-heartbeat");
            t.setDaemon(true);
            return t;
        });

        ntpExecutor.scheduleAtFixedRate(() -> 
            ntpClient.pollDetailed(ntpPool).ifPresent(response -> {
                lastNtp.set(response);
                logger.info("NTP Heartbeat: {} | Stratum: {} | RTT: {}ms", 
                    response.time(), response.stratum(), response.rttMs());
            }), 0, 60, TimeUnit.SECONDS);

        final NmeaSentenceAccumulator accumulator = new NmeaSentenceAccumulator();
        final NmeaParser parser = new NmeaParser();
        final SerialConnector connector = new SerialConnector(config, accumulator, provider);
        final AtomicReference<GpsData> currentFix = new AtomicReference<>(new GpsData(null, null, 0, 0, 0, 0));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received. Closing resources...");
            connector.disconnect();
            ntpExecutor.shutdown();
        }));

        logger.info("Connecting to {}...", port);

        connector.connect(port)
            .map(s -> TelemetryPulse.start(s, lastNtp.get()))
            .peek(p -> Map.<Boolean, Runnable>of(
                true, () -> p.logRaw(logger),
                false, () -> {}
            ).get(showRaw).run())
            .map(p -> p.update(parser, currentFix))
            .filter(TelemetryPulse::hasValidFix)
            .forEach(p -> p.logFinal(logger));
    }

    static record TelemetryPulse(String pulseId, String sentence, GpsData data, NtpResponse reference) {
        
        static TelemetryPulse start(final String sentence, final NtpResponse ntp) {
            final String id = String.format("%04X", (sentence.hashCode() & 0xFFFF));
            return new TelemetryPulse(id, sentence, null, ntp);
        }

        void logRaw(final Logger log) {
            runWithContext(() -> log.debug("[RAW] {}", sentence));
        }

        TelemetryPulse update(final NmeaParser parser, final AtomicReference<GpsData> state) {
            final GpsData next = state.updateAndGet(fix -> parser.parse(sentence, fix));
            return new TelemetryPulse(pulseId, sentence, next, reference);
        }

        boolean hasValidFix() {
            return Optional.ofNullable(data)
                .flatMap(d -> Optional.ofNullable(d.utcTime()))
                .isPresent();
        }

        void logFinal(final Logger log) {
            runWithContext(() -> {
                final String grid = GridSquareCalculator.calculate(data.latitude(), data.longitude());
                final String ntpStatus = Optional.ofNullable(reference)
                    .map(r -> String.format("NTP: %s (RTT: %dms, Stratum: %d)", r.time(), r.rttMs(), r.stratum()))
                    .orElse("NTP: No Reference");
                
                log.info("GPS Fix: {} | {} | Grid: {}", data, ntpStatus, grid);
            });
        }

        private void runWithContext(final Runnable action) {
            MDC.put("pulseId", pulseId);
            try {
                action.run();
            } finally {
                MDC.clear();
            }
        }
    }
}
