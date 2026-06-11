package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.InstantSource;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Integration test focusing on the 'Connection Neutralization' recovery lifecycle.
 * Adheres to strict AssertJ fluent assertion standards and Java 21 unnamed parameters.
 * Certified for Phase 8 Temporal Virtualization.
 */
class SystemRecoveryIntegrationTest extends BddTest {

    private static final int RECOVERY_WAIT_SECONDS = 5;
    private static final int THREAD_JOIN_TIMEOUT_MS = 1000;
    private static final int LATCH_TIMEOUT_SECONDS = 10;

    @TempDir
    private Path tempDir;

    @Test
    void should_reacquire_hardware_after_neutralization_lifecycle() throws Exception {
        // GIVEN: A configuration and hardware that fails then restores
        final Path configPath = tempDir.resolve("recovery-cycle.properties");
        java.nio.file.Files.writeString(configPath, "simulation.mode=false");
        final ConfigManager configManager = new ConfigManager(configPath);
        
        final ISerialProvider mockProvider = mock(ISerialProvider.class);
        final ISerialPort mockPort = mock(ISerialPort.class);
        
        final CountDownLatch neutralizationLatch = new CountDownLatch(1);
        final CountDownLatch reacquisitionLatch = new CountDownLatch(2);

        // Initial state: Hardware available
        given(mockProvider.getAvailablePorts()).willReturn(List.of(mockPort));
        given(mockProvider.getPort(anyString())).willReturn(mockPort);
        given(mockPort.openPort()).willAnswer(inv -> {
            reacquisitionLatch.countDown();
            return true;
        });
        given(mockPort.isOpen()).willReturn(true);
        given(mockPort.getSystemPortName()).willReturn("COM_RECOVERY");
        
        // Count down when closePort is called
        doAnswer(inv -> {
            neutralizationLatch.countDown();
            return true;
        }).when(mockPort).closePort();

        final SystemOrchestrator orchestrator = new SystemOrchestrator(
            configManager, 
            mockProvider, 
            null, 
            InstantSource.system(),
            new com.stoicprogrammer.qtrqth.sentinel.NoOpSentinel()
        );

        // WHEN: The system starts (Named parameter to avoid preview features)
        final Thread engineThread = new Thread(() -> orchestrator.start(pulse -> {}));
        engineThread.setDaemon(true);
        engineThread.start();

        // THEN: The system should eventually neutralize the port due to lack of data (Watchdog timeout)
        assertThat(neutralizationLatch.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS))
            .as("System failed to neutralize stale port after watchdog timeout")
            .isTrue();

        // AND: The system should attempt to re-acquire the port
        assertThat(reacquisitionLatch.await(LATCH_TIMEOUT_SECONDS, TimeUnit.SECONDS))
            .as("System failed to attempt re-acquisition after neutralization")
            .isTrue();

        orchestrator.shutdown();
        engineThread.join(THREAD_JOIN_TIMEOUT_MS);

        verify(mockPort, atLeastOnce()).closePort();
        verify(mockPort, atLeast(2)).openPort();
    }
}
