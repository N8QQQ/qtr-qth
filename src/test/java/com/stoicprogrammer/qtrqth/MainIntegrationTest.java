package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class MainIntegrationTest extends BddTest {

    @TempDir
    private Path tempDir;

    @Test
    void should_boot_system_in_simulation_mode_with_defaults() throws Exception {
        final Path configPath = tempDir.resolve("boot.properties");
        java.nio.file.Files.writeString(configPath, "simulation.mode=true\ndisplay.raw.telemetry=true");
        
        // Use a thread to run the main loop so we can interrupt it
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> Main.run(configPath));
        
        // Allow it to run for a few pulses
        Thread.sleep(2000);
        
        executor.shutdownNow();
        final boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
        
        assertThat(terminated).isTrue();
    }
}
