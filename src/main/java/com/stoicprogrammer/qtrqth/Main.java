package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.serial.HardwareProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * qtr-qth: GPS Time & Location Sync for Amateur Radio.
 */
public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_CONFIG_FILENAME = "qtr-qth.properties";
    private static final String PROBE_FLAG = "--probe";
    private static final String DOCTOR_FLAG = "--doctor";
    private static final String CAPTURE_FLAG = "--capture";

    private Main() {
        // Utility Class
    }

    /**
     * Entry point for the application.
     * Supports high-fidelity hardware probing, environment diagnostics, and telemetry capture.
     */
    public static void main(final String[] args) {
        final List<String> argList = List.of(args);
        
        // Declarative Routing Table
        List.<RoutingRule>of(
            new RoutingRule(argList.contains(PROBE_FLAG), () -> runProbe(argList)),
            new RoutingRule(argList.contains(DOCTOR_FLAG), Main::runDoctor),
            new RoutingRule(argList.contains(CAPTURE_FLAG), () -> runCapture(argList)),
            new RoutingRule(true, () -> runOrchestrator(argList))
        ).stream()
         .filter(rule -> rule.condition)
         .findFirst()
         .ifPresent(rule -> rule.action.run());
    }

    private record RoutingRule(boolean condition, Runnable action) {}

    private static void runProbe(final List<String> args) {
        HardwareProbe.main(args.toArray(String[]::new));
    }

    private static void runDoctor() {
        com.stoicprogrammer.qtrqth.util.EnvironmentDoctor.performCheck();
    }

    private static void runCapture(final List<String> args) {
        final String configPath = args.stream()
            .filter(s -> !s.equals(CAPTURE_FLAG))
            .findFirst()
            .orElse(DEFAULT_CONFIG_FILENAME);

        final String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        final java.nio.file.Path capturePath = java.nio.file.Path.of("telemetry_capture_" + timestamp + ".nmea");

        logger.info("STRATUM 0 AUDIT: Initiating high-speed telemetry capture...");
        
        try (var captor = new com.stoicprogrammer.qtrqth.util.TelemetryCaptor(capturePath)) {
            final SystemOrchestrator orchestrator = new SystemOrchestrator(Path.of(configPath));

            // Registry shutdown hook for graceful cleanup
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Capture shutdown signal received.");
                orchestrator.shutdown();
            }, "capture-shutdown-hook"));

            // Boot the system with capture listener
            orchestrator.start(pulse -> pulse.logFinal(logger), captor::capture);
        }
    }

    private static void runOrchestrator(final List<String> args) {
        final String configPath = args.stream()
            .findFirst()
            .orElse(DEFAULT_CONFIG_FILENAME);

        final SystemOrchestrator orchestrator = new SystemOrchestrator(Path.of(configPath));

        // Registry shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received.");
            orchestrator.shutdown();
        }, "shutdown-hook"));

        // Boot the system
        orchestrator.start(pulse -> pulse.logFinal(logger));
    }
}
