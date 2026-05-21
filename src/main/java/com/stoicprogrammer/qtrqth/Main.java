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

    private Main() {
        // Utility Class
    }

    /**
     * Entry point for the application.
     * Supports high-fidelity hardware probing and environment diagnostics.
     */
    public static void main(final String[] args) {
        final List<String> argList = List.of(args);
        
        // Prioritized Routing Table
        if (argList.contains(PROBE_FLAG)) {
            runProbe(argList);
        } else if (argList.contains(DOCTOR_FLAG)) {
            runDoctor();
        } else {
            runOrchestrator(argList);
        }
    }

    private static void runProbe(final List<String> args) {
        HardwareProbe.main(args.toArray(String[]::new));
    }

    private static void runDoctor() {
        com.stoicprogrammer.qtrqth.util.EnvironmentDoctor.performCheck();
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
