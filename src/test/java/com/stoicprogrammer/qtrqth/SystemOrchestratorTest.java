package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * High-fidelity certification of the System Orchestrator.
 * Verifies the full 'Confluence' lifecycle in a view-agnostic manner.
 */
class SystemOrchestratorTest extends BddTest {

    private static final int POLL_INTERVAL_MS = 200;
    private static final int MAX_POLL_ATTEMPTS = 50;
    private static final int SHUTDOWN_WAIT_MS = 2000;

    @TempDir
    private Path tempDir;

    @Test
    void should_orchestrate_telemetry_flow_and_produce_pulses() throws Exception {
        final Path configPath = tempDir.resolve("orchestrator.properties");
        // Force simulation mode and long threshold to ensure stability in test
        java.nio.file.Files.writeString(configPath, "simulation.mode=true\nsync.threshold.ms=5000");
        
        final SystemOrchestrator orchestrator = new SystemOrchestrator(configPath);
        final List<TelemetryPulse> capturedPulses = new CopyOnWriteArrayList<>();

        // Start the engine with a simple list collector as the 'View'
        final Thread engineThread = new Thread(() -> orchestrator.start(capturedPulses::add));
        engineThread.start();

        // Functional Polling: Wait for at least one pulse
        Stream.generate(() -> {
            try { Thread.sleep(POLL_INTERVAL_MS); } 
            catch (final InterruptedException e) { Thread.currentThread().interrupt(); }
            return capturedPulses.isEmpty();
        }).limit(MAX_POLL_ATTEMPTS).takeWhile(empty -> empty).count();

        orchestrator.shutdown();
        engineThread.join(SHUTDOWN_WAIT_MS);

        assertThat(capturedPulses).isNotEmpty();
    }

    @Test
    void should_handle_shutdown_gracefully_even_if_not_started() {
        final SystemOrchestrator orchestrator = new SystemOrchestrator(tempDir.resolve("empty.properties"));
        orchestrator.shutdown();
        // Should not throw NPE or block
    }
}
