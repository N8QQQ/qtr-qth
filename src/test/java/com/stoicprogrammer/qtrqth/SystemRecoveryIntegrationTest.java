package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Integration test focusing on the 'Connection Neutralization' recovery lifecycle.
 */
class SystemRecoveryIntegrationTest extends BddTest {

    private static final int RECOVERY_WAIT_SECONDS = 5;
    private static final int THREAD_JOIN_TIMEOUT_MS = 1000;

    @TempDir
    private Path tempDir;

    @Test
    void should_neutralize_stale_connection_during_recovery_cycle() throws Exception {
        // GIVEN: A configuration and mocked hardware
        final Path configPath = tempDir.resolve("recovery.properties");
        java.nio.file.Files.writeString(configPath, "simulation.mode=false");
        final ConfigManager configManager = new ConfigManager(configPath);
        
        final ISerialProvider mockProvider = mock(ISerialProvider.class);
        final ISerialPort mockPort = mock(ISerialPort.class);
        
        given(mockProvider.getAvailablePorts()).willReturn(List.of(mockPort));
        given(mockProvider.getPort(anyString())).willReturn(mockPort);
        given(mockPort.openPort()).willReturn(true);
        given(mockPort.isOpen()).willReturn(true);
        given(mockPort.getSystemPortName()).willReturn("COM_TEST");
        
        // Count how many times initConfluence tries to start
        final AtomicInteger confluenceStarts = new AtomicInteger(0);
        final CountDownLatch recoveryLatch = new CountDownLatch(2);

        final SystemOrchestrator orchestrator = new SystemOrchestrator(configManager, mockProvider, null);

        // WHEN: The system starts
        final Thread engineThread = new Thread(() -> orchestrator.start(pulse -> {
            confluenceStarts.incrementAndGet();
            // Simulate a single pulse then "signal loss" by making the next poll return null
        }));
        engineThread.setDaemon(true);
        engineThread.start();

        // Wait a bit for the first cycle to run and fail (it will fail because we provide no data)
        // The failure will trigger executeConfluenceCycle's recovery block.
        final boolean reachedRecovery = recoveryLatch.await(RECOVERY_WAIT_SECONDS, TimeUnit.SECONDS);

        orchestrator.shutdown();
        engineThread.join(THREAD_JOIN_TIMEOUT_MS);

        // THEN: The connector.disconnect() should have been called, which calls port.closePort()
        // We verify that closePort was called at least once during the recovery attempts.
        verify(mockPort, atLeastOnce()).closePort();
        verify(mockPort, atLeastOnce()).removeDataListener();
    }
}
