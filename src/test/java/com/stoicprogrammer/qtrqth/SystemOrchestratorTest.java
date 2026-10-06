package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

class SystemOrchestratorTest extends BddTest {
    private static final Logger logger = LoggerFactory.getLogger(SystemOrchestratorTest.class);
    private static final Instant MOCK_TIME = Instant.parse("2026-05-21T12:34:56.00Z");
    private static final long LATCH_TIMEOUT_SECONDS = 5L;

    @TempDir
    private Path tempDir;

    private final OrchestratorFixture fixture = new OrchestratorFixture();

    @Test
    void should_automatically_fallback_to_simulation_when_hardware_missing() throws Exception {
        logger.info("Starting BDD Test: should_automatically_fallback_to_simulation_when_hardware_missing");
        final Path configPath = tempDir.resolve("missing-hardware.properties");
        fixture.given_config(configPath, "simulation.mode=false\nsync.threshold.ms=5000");
        fixture.given_empty_serial_provider();
        fixture.given_frozen_clock(MOCK_TIME);
        fixture.given_noop_sentinel();
        fixture.when_building_orchestrator();

        final CountDownLatch pulseLatch = new CountDownLatch(1);
        fixture.when_starting(pulseLatch);

        fixture.then_latch_completes_within(pulseLatch, LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS, "simulation fallback pulses");
        fixture.when_shutting_down();

        fixture.then_pulses_captured_is_not_empty();
        fixture.then_first_pulse_ingress_time_is(MOCK_TIME);
    }

    @Test
    void should_handle_shutdown_gracefully_even_if_not_started() throws InterruptedException {
        fixture.when_building_orchestrator(tempDir.resolve("empty.properties"));
        fixture.when_shutting_down();
    }
}
