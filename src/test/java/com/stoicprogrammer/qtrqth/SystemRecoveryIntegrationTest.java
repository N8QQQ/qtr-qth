package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Integration test focusing on the 'Connection Neutralization' recovery lifecycle.
 * Adheres to strict AssertJ fluent assertion standards and Given-When-Then fixtures.
 */
class SystemRecoveryIntegrationTest extends BddTest {

    private static final long LATCH_TIMEOUT_SECONDS = 10L;
    private static final int EXPECTED_OPEN_ATTEMPTS = 2;

    @TempDir
    private Path tempDir;

    private final OrchestratorFixture fixture = new OrchestratorFixture();

    @Test
    void should_reacquire_hardware_after_neutralization_lifecycle() throws Exception {
        final Path configPath = tempDir.resolve("recovery-cycle.properties");
        fixture.given_config(configPath, "simulation.mode=false");

        final ISerialProvider mockProvider = mock(ISerialProvider.class);
        final ISerialPort mockPort = mock(ISerialPort.class);
        final CountDownLatch neutralizationLatch = new CountDownLatch(1);
        final CountDownLatch reacquisitionLatch = new CountDownLatch(EXPECTED_OPEN_ATTEMPTS);

        given(mockProvider.getAvailablePorts()).willReturn(List.of(mockPort));
        given(mockProvider.getPort(anyString())).willReturn(mockPort);
        given(mockPort.openPort()).willAnswer(inv -> {
            reacquisitionLatch.countDown();
            return true;
        });
        given(mockPort.isOpen()).willReturn(true);
        given(mockPort.getSystemPortName()).willReturn("COM_RECOVERY");

        doAnswer(inv -> {
            neutralizationLatch.countDown();
            return true;
        }).when(mockPort).closePort();

        fixture.given_serial_provider(mockProvider);
        fixture.given_noop_sentinel();
        fixture.when_building_orchestrator();
        fixture.when_starting(pulse -> {});

        fixture.then_latch_completes_within(
            neutralizationLatch,
            LATCH_TIMEOUT_SECONDS,
            TimeUnit.SECONDS,
            "neutralize stale port after watchdog timeout"
        );

        fixture.then_latch_completes_within(
            reacquisitionLatch,
            LATCH_TIMEOUT_SECONDS,
            TimeUnit.SECONDS,
            "attempt re-acquisition after neutralization"
        );

        fixture.when_shutting_down();

        fixture.then_port_closed_at_least_once(mockPort);
        fixture.then_port_opened_at_least(mockPort, EXPECTED_OPEN_ATTEMPTS);
    }
}
