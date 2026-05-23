package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.time.Instant;
import java.time.InstantSource;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * High-fidelity certification of the System Orchestrator.
 * Verifies the full 'Confluence' lifecycle in a view-agnostic manner.
 * Adheres to deterministic frozen clock verification (Phase 8).
 */
class SystemOrchestratorTest extends BddTest {

    private static final int POLL_INTERVAL_MS = 500;
    private static final int MAX_POLL_ATTEMPTS = 120; // 60 seconds total
    private static final int SHUTDOWN_WAIT_MS = 10000;
    private static final Instant MOCK_TIME = Instant.parse("2026-05-21T12:00:00.00Z");

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SystemOrchestratorTest.class);

    @TempDir
    private Path tempDir;

    @Test
    void should_orchestrate_telemetry_flow_and_produce_pulses() throws Exception {
        logger.info("Starting BDD Test: should_orchestrate_telemetry_flow_and_produce_pulses");
        final Path configPath = tempDir.resolve("orchestrator.properties");
        // Force simulation mode and rapid calibration for test
        java.nio.file.Files.writeString(configPath, "simulation.mode=true\nsync.threshold.ms=5000\nsync.calibration.cycles=1");
        
        final InstantSource frozenClock = InstantSource.fixed(MOCK_TIME);
        final SystemOrchestrator orchestrator = new SystemOrchestrator(new ConfigManager(configPath), null, null, frozenClock);
        final List<TelemetryPulse> capturedPulses = new CopyOnWriteArrayList<>();

        // Start the engine with a simple list collector as the 'View'
        final Thread engineThread = new Thread(() -> orchestrator.start(pulse -> {
            logger.info("Captured Pulse: {} [Ingress: {}]", pulse.pulseId(), pulse.ingressTime());
            capturedPulses.add(pulse);
        }));
        engineThread.setDaemon(true);
        engineThread.start();

        // Functional Polling: Wait for at least one pulse
        final long startTime = System.currentTimeMillis();
        
        Stream.generate(() -> {
            try { 
                Thread.sleep(POLL_INTERVAL_MS); 
            } catch (final InterruptedException e) { 
                Thread.currentThread().interrupt(); 
            }
            return capturedPulses.isEmpty();
        })
        .limit(MAX_POLL_ATTEMPTS)
        .takeWhile(empty -> empty)
        .forEach(empty -> {});

        logger.info("Polling finished after {}ms. Pulse received: {}", System.currentTimeMillis() - startTime, !capturedPulses.isEmpty());

        orchestrator.shutdown();
        engineThread.join(SHUTDOWN_WAIT_MS);

        assertThat(capturedPulses).as("System failed to produce a synchronized pulse within the timeout").isNotEmpty();
        assertThat(capturedPulses.get(0).ingressTime()).isEqualTo(MOCK_TIME);
    }

    @Test
    void should_automatically_fallback_to_simulation_when_hardware_missing() throws Exception {
        logger.info("Starting BDD Test: should_automatically_fallback_to_simulation_when_hardware_missing");
        final Path configPath = tempDir.resolve("fallback.properties");
        
        // GIVEN: Intent is Hardware Mode, but no physical hardware will be found in CI
        java.nio.file.Files.writeString(configPath, "simulation.mode=false\nsync.threshold.ms=5000");
        
        final InstantSource frozenClock = InstantSource.fixed(MOCK_TIME);
        final SystemOrchestrator orchestrator = new SystemOrchestrator(new ConfigManager(configPath), null, null, frozenClock);
        final List<TelemetryPulse> capturedPulses = new CopyOnWriteArrayList<>();

        // WHEN: The system boots
        final Thread engineThread = new Thread(() -> orchestrator.start(capturedPulses::add));
        engineThread.setDaemon(true);
        engineThread.start();

        // THEN: Adaptive Fallback should engage and produce pulses from the simulation provider
        Stream.generate(() -> {
            try { 
                Thread.sleep(POLL_INTERVAL_MS); 
            } catch (final InterruptedException e) { 
                Thread.currentThread().interrupt(); 
            }
            return capturedPulses.isEmpty();
        }).limit(MAX_POLL_ATTEMPTS).takeWhile(empty -> empty).count();

        orchestrator.shutdown();
        engineThread.join(SHUTDOWN_WAIT_MS);

        assertThat(capturedPulses).as("System should have failed over to simulation and produced pulses").isNotEmpty();
        assertThat(capturedPulses.get(0).ingressTime()).isEqualTo(MOCK_TIME);
    }

    @Test
    void should_handle_shutdown_gracefully_even_if_not_started() {
        final SystemOrchestrator orchestrator = new SystemOrchestrator(tempDir.resolve("empty.properties"));
        orchestrator.shutdown();
        // Should not throw NPE or block
    }
}
