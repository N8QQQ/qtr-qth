package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the Main entry point.
 */
class MainIntegrationTest extends BddTest {

    private static final int BOOT_DELAY_MS = 2000;
    private static final int TERMINATION_TIMEOUT_SECONDS = 5;

    @TempDir
    private Path tempDir;

    @Test
    void should_boot_system_in_simulation_mode_with_defaults() throws Exception {
        final Path configPath = tempDir.resolve("boot.properties");
        java.nio.file.Files.writeString(configPath, "simulation.mode=true\ndisplay.raw.telemetry=true");
        
        // Use a thread to run the main loop so we can interrupt it
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        
        // Use SystemOrchestrator directly since Main is a thin wrapper
        final SystemOrchestrator orchestrator = new SystemOrchestrator(configPath);
        executor.submit(() -> orchestrator.start(pulse -> {}));
        
        // Allow it to run for a few pulses
        Thread.sleep(BOOT_DELAY_MS);
        
        orchestrator.shutdown();
        executor.shutdownNow();
        final boolean terminated = executor.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        assertThat(terminated).isTrue();
    }
}
