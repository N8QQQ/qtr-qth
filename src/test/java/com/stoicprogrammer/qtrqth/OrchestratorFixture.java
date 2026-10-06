package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import com.stoicprogrammer.qtrqth.ntp.api.INtpProvider;
import com.stoicprogrammer.qtrqth.sentinel.NoOpSentinel;
import com.stoicprogrammer.qtrqth.sentinel.api.IStreamSentinel;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.mockito.BDDMockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.InstantSource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Shared BDD Fixture for SystemOrchestrator lifecycle and integration testing.
 * Package-private to com.stoicprogrammer.qtrqth to allow direct access to
 * SystemOrchestrator's internal testing constructor without exposing it publicly.
 */
public final class OrchestratorFixture {
    private static final long THREAD_JOIN_TIMEOUT_MS = 5000L;

    private ConfigManager configManager;
    private ISerialProvider serialProvider;
    private INtpProvider ntpProvider;
    private InstantSource clock = InstantSource.system();
    private IStreamSentinel sentinel = new NoOpSentinel();

    private Optional<SystemOrchestrator> orchestrator = Optional.empty();
    private Optional<Thread> engineThread = Optional.empty();
    private final List<TelemetryPulse> capturedPulses = new CopyOnWriteArrayList<>();

    public void given_config(final Path configPath, final String content) throws IOException {
        Files.writeString(configPath, content);
        this.configManager = new ConfigManager(configPath);
    }

    public void given_empty_serial_provider() {
        this.serialProvider = mock(ISerialProvider.class);
        BDDMockito.given(serialProvider.getAvailablePorts()).willReturn(List.of());
    }

    public void given_frozen_clock(final Instant time) {
        this.clock = InstantSource.fixed(time);
    }

    public void given_noop_sentinel() {
        this.sentinel = new NoOpSentinel();
    }

    public void given_serial_provider(final ISerialProvider provider) {
        this.serialProvider = provider;
    }

    public void when_building_orchestrator() {
        this.orchestrator = Optional.of(new SystemOrchestrator(
            configManager,
            serialProvider,
            ntpProvider,
            clock,
            sentinel
        ));
    }

    public void when_building_orchestrator(final Path configPath) {
        this.orchestrator = Optional.of(new SystemOrchestrator(configPath));
    }

    public void when_starting(final CountDownLatch latch) {
        if (orchestrator.isEmpty()) {
            when_building_orchestrator();
        }
        final Thread thread = new Thread(() -> orchestrator.ifPresent(o -> o.start(pulse -> {
            capturedPulses.add(pulse);
            latch.countDown();
        })));
        thread.setDaemon(true);
        thread.start();
        this.engineThread = Optional.of(thread);
    }

    public void when_starting(final Consumer<TelemetryPulse> consumer) {
        if (orchestrator.isEmpty()) {
            when_building_orchestrator();
        }
        final Thread thread = new Thread(() -> orchestrator.ifPresent(o -> o.start(consumer)));
        thread.setDaemon(true);
        thread.start();
        this.engineThread = Optional.of(thread);
    }

    public void when_shutting_down() throws InterruptedException {
        orchestrator.ifPresent(SystemOrchestrator::shutdown);
        if (engineThread.isPresent()) {
            engineThread.get().join(THREAD_JOIN_TIMEOUT_MS);
        }
    }

    public void then_pulses_captured_is_not_empty() {
        assertThat(capturedPulses).as("System should have captured pulses").isNotEmpty();
    }

    public void then_first_pulse_ingress_time_is(final Instant expectedTime) {
        assertThat(capturedPulses).isNotEmpty();
        assertThat(capturedPulses.get(0).ingressTime()).isEqualTo(expectedTime);
    }

    public void then_latch_completes_within(
        final CountDownLatch latch,
        final long timeout,
        final TimeUnit unit,
        final String description
    ) throws InterruptedException {
        final boolean completed = latch.await(timeout, unit);
        assertThat(completed)
            .as("Timed out waiting for %s within %d %s", description, timeout, unit)
            .isTrue();
    }

    public void then_port_closed_at_least_once(final ISerialPort port) {
        verify(port, atLeastOnce()).closePort();
    }

    public void then_port_opened_at_least(final ISerialPort port, final int times) {
        verify(port, atLeast(times)).openPort();
    }
}
