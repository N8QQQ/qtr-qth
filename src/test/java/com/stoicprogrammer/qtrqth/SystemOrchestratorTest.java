package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.time.InstantSource;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SystemOrchestratorTest extends BddTest {
    private static final Logger logger = LoggerFactory.getLogger(SystemOrchestratorTest.class);
    private static final Instant MOCK_TIME = Instant.parse("2026-05-21T12:34:56.00Z");
    private static final int POLL_INTERVAL_MS = 100;
    private static final int MAX_POLL_ATTEMPTS = 50;
    private static final int SHUTDOWN_WAIT_MS = 5000;

    @TempDir
    private Path tempDir;

    @Test
    void should_automatically_fallback_to_simulation_when_hardware_missing() throws Exception {
        logger.info("Starting BDD Test: should_automatically_fallback_to_simulation_when_hardware_missing");
        
        // GIVEN: A config path that does not exist, forcing default discovery behavior
        final Path configPath = tempDir.resolve("missing-hardware.properties");
        java.nio.file.Files.writeString(configPath, "simulation.mode=false\nsync.threshold.ms=5000");
        
        // Mock an empty serial provider to force discovery failure regardless of host hardware
        final com.stoicprogrammer.qtrqth.serial.api.ISerialProvider emptyProvider = 
            org.mockito.Mockito.mock(com.stoicprogrammer.qtrqth.serial.api.ISerialProvider.class);
        org.mockito.BDDMockito.given(emptyProvider.getAvailablePorts()).willReturn(List.of());

        final InstantSource frozenClock = InstantSource.fixed(MOCK_TIME);
        final SystemOrchestrator orchestrator = new SystemOrchestrator(
            new ConfigManager(configPath), 
            emptyProvider, 
            null, 
            frozenClock,
            new com.stoicprogrammer.qtrqth.sentinel.NoOpSentinel()
        );
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
