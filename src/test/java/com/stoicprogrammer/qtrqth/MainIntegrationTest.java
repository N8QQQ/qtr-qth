package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the Main entry point.
 */
class MainIntegrationTest extends BddTest {

    private static final long PULSE_TIMEOUT_SECONDS = 3L;
    private static final long TERMINATION_TIMEOUT_SECONDS = 5L;

    @TempDir
    private Path tempDir;

    private final CliFixture fixture = new CliFixture();

    @Test
    void should_boot_system_in_simulation_mode_with_defaults() throws Exception {
        fixture.given_simulation_config(tempDir);
        fixture.when_booting_orchestrator();
        fixture.then_pulse_received_within(PULSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        fixture.then_shutdown_terminates_within(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private final class CliFixture {
        private Path configPath;
        private SystemOrchestrator orchestrator;
        private ExecutorService executor;
        private final CountDownLatch pulseLatch = new CountDownLatch(1);

        void given_simulation_config(final Path dir) throws IOException {
            this.configPath = dir.resolve("boot.properties");
            Files.writeString(configPath, "simulation.mode=true\ndisplay.raw.telemetry=true");
        }

        void when_booting_orchestrator() {
            this.orchestrator = new SystemOrchestrator(configPath);
            this.executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> orchestrator.start(pulse -> pulseLatch.countDown()));
        }

        void then_pulse_received_within(final long timeout, final TimeUnit unit) throws InterruptedException {
            final boolean received = pulseLatch.await(timeout, unit);
            assertThat(received)
                .as("Timed out waiting for initial pulse emission within %d %s", timeout, unit)
                .isTrue();
        }

        void then_shutdown_terminates_within(final long timeout, final TimeUnit unit) throws InterruptedException {
            orchestrator.shutdown();
            executor.shutdownNow();
            final boolean terminated = executor.awaitTermination(timeout, unit);
            assertThat(terminated)
                .as("Timed out waiting for executor termination within %d %s", timeout, unit)
                .isTrue();
        }
    }
}
