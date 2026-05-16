package com.stoicprogrammer.qtrqth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Optional;

/**
 * qtr-qth: GPS Time & Location Sync for Amateur Radio.
 */
public final class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String DEFAULT_CONFIG_FILENAME = "qtr-qth.properties";

    private Main() {
        // Utility Class
    }

    /**
     * Entry point for the application.
     * Delegates all logical orchestration to the SystemOrchestrator.
     */
    public static void main(final String[] args) {
        final String configPath = Optional.ofNullable(args)
            .filter(a -> a.length > 0)
            .map(a -> a[0])
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
