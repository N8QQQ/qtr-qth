package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.config.AppConfig;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.model.ConfluenceHealth;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator;
import com.stoicprogrammer.qtrqth.ntp.NtpClient;
import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import com.stoicprogrammer.qtrqth.ntp.api.INtpProvider;
import com.stoicprogrammer.qtrqth.ntp.network.NetworkNtpProvider;
import com.stoicprogrammer.qtrqth.ntp.simulation.SimulationNtpProvider;
import com.stoicprogrammer.qtrqth.serial.PortDiscovery;
import com.stoicprogrammer.qtrqth.serial.SerialConnector;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import com.stoicprogrammer.qtrqth.serial.jserialcomm.JSerialCommProvider;
import com.stoicprogrammer.qtrqth.serial.simulation.SimulationSerialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * High-fidelity orchestrator for the qtr-qth telemetry hub.
 * Decouples system initialization and pipeline execution from the static Main entry point.
 */
public final class SystemOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(SystemOrchestrator.class);

    private static final int NTP_TIMEOUT_MS = 5000;
    private static final int NTP_POLL_INTERVAL_SECONDS = 60;
    private static final int RECOVERY_BACKOFF_MS = 2000;

    private final ConfigManager configManager;
    private final ScheduledExecutorService ntpExecutor;
    private final AtomicReference<NtpResponse> lastNtp = new AtomicReference<>();
    private final AtomicReference<GpsData> currentFix = new AtomicReference<>(new GpsData(null, null, 0, 0, 0, 0));
    private final AtomicReference<ConfluenceHealth> healthState = new AtomicReference<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    
    // Testing Injectors
    private final ISerialProvider testSerialProvider;
    private final INtpProvider testNtpProvider;

    private SerialConnector connector;

    public SystemOrchestrator(final Path configPath) {
        this(new ConfigManager(configPath), null, null);
    }

    /**
     * Internal constructor for high-fidelity testing and dependency injection.
     */
    SystemOrchestrator(final ConfigManager configManager, final ISerialProvider serialProvider, final INtpProvider ntpProvider) {
        this.configManager = configManager;
        this.testSerialProvider = serialProvider;
        this.testNtpProvider = ntpProvider;
        this.ntpExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "ntp-heartbeat");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Executes the full telemetry confluence pipeline with self-healing recovery.
     * @param pulseConsumer A consumer for processed pulses (e.g., logging or UI updates).
     */
    public void start(final Consumer<TelemetryPulse> pulseConsumer) {
        final AppConfig config = configManager.getConfig();
        
        // 1. Resolve Operational Mode (State-Lock)
        final ConfluenceHealth.OperationalMode mode = resolveMode(config);
        
        this.healthState.set(new ConfluenceHealth(
            ConfluenceHealth.RiverStatus.ACTIVE, 
            ConfluenceHealth.RiverStatus.ACTIVE, 
            mode
        ));

        logger.info("System bootstrapping... (Mode: {})", mode);

        // 2. Network Time Heartbeat
        initNtpHeartbeat(config, mode == ConfluenceHealth.OperationalMode.SIMULATION_LOCK);

        // 3. Adaptive Recovery Stream
        // We use a generating stream to replace the forbidden 'while' loop.
        Stream.generate(running::get)
            .takeWhile(Boolean::booleanValue)
            .forEach(r -> executeConfluenceCycle(mode, pulseConsumer));
    }

    private void executeConfluenceCycle(final ConfluenceHealth.OperationalMode mode, final Consumer<TelemetryPulse> pulseConsumer) {
        initConfluence(mode == ConfluenceHealth.OperationalMode.SIMULATION_LOCK, pulseConsumer);
        
        // If we reach this point, the serial stream has collapsed.
        if (running.get()) {
            logger.warn("SIGNAL LOSS DETECTED: Entering Adaptive Recovery...");
            updateGpsHealth(true);
            Optional.ofNullable(connector).ifPresent(SerialConnector::disconnect);
            sleep(RECOVERY_BACKOFF_MS);
        }
    }

    private ConfluenceHealth.OperationalMode resolveMode(final AppConfig config) {
        if (config.simulationMode()) {
            return ConfluenceHealth.OperationalMode.SIMULATION_LOCK;
        }

        final List<String> physicalPorts = new PortDiscovery(new JSerialCommProvider(), configManager).getAvailablePorts();
        if (physicalPorts.isEmpty()) {
            logger.warn("STRATUM 0 DISCOVERY FAILURE: No physical serial hardware identified.");
            logger.info("ADAPTIVE FALLBACK: Locking into Simulation Mode for this run.");
            return ConfluenceHealth.OperationalMode.SIMULATION_LOCK;
        }

        return ConfluenceHealth.OperationalMode.HARDWARE_LOCK;
    }

    private void initNtpHeartbeat(final AppConfig config, final boolean useSimulation) {
        final INtpProvider provider = Optional.ofNullable(testNtpProvider)
            .orElseGet(() -> useSimulation ? new SimulationNtpProvider() : new NetworkNtpProvider());
        final NtpClient client = new NtpClient(provider, NTP_TIMEOUT_MS);
        
        ntpExecutor.scheduleAtFixedRate(() -> {
            final Optional<NtpResponse> response = client.pollDetailed(config.ntpPool());
            updateNtpHealth(response.isPresent());
            response.ifPresent(lastNtp::set);
        }, 0, NTP_POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void updateNtpHealth(final boolean active) {
        healthState.updateAndGet(h -> new ConfluenceHealth(
            h.gpsStatus(),
            active ? ConfluenceHealth.RiverStatus.ACTIVE : ConfluenceHealth.RiverStatus.RECOVERY,
            h.mode()
        ));
    }

    private void initConfluence(final boolean useSimulation, final Consumer<TelemetryPulse> pulseConsumer) {
        final ISerialProvider provider = Optional.ofNullable(testSerialProvider)
            .orElseGet(() -> useSimulation ? new SimulationSerialProvider() : new JSerialCommProvider());
        final PortDiscovery discovery = new PortDiscovery(provider, configManager);
        
        discovery.findLikelyGpsPort()
            .or(() -> discovery.getAvailablePorts().stream().findFirst())
            .ifPresentOrElse(
                port -> runPipeline(port, provider, pulseConsumer),
                () -> {
                    if (!useSimulation) {
                        logger.debug("Monitoring for hardware restoration...");
                    }
                }
            );
    }

    private void runPipeline(final String port, final ISerialProvider provider, final Consumer<TelemetryPulse> consumer) {
        final AppConfig config = configManager.getConfig();
        final NmeaParser parser = new NmeaParser();
        this.connector = new SerialConnector(configManager, new NmeaSentenceAccumulator(), provider);

        logger.info("Telemetry Confluence initiated on port {}...", port);
        updateGpsHealth(false);

        connector.connect(port)
            .map(sentence -> TelemetryPulse.start(sentence, lastNtp.get(), healthState.get()))
            .peek(pulse -> Map.<Boolean, Runnable>of(
                true, () -> pulse.logRaw(logger),
                false, () -> {}
            ).get(config.displayRawTelemetry()).run())
            .map(pulse -> pulse.update(parser, currentFix))
            .filter(TelemetryPulse::hasValidFix)
            .forEach(consumer);
    }

    private void updateGpsHealth(final boolean signalLoss) {
        healthState.updateAndGet(h -> new ConfluenceHealth(
            signalLoss ? ConfluenceHealth.RiverStatus.RECOVERY : ConfluenceHealth.RiverStatus.ACTIVE,
            h.ntpStatus(),
            h.mode()
        ));
    }

    private void sleep(final int ms) {
        try { 
            Thread.sleep(ms); 
        } catch (final InterruptedException e) { 
            Thread.currentThread().interrupt(); 
        }
    }

    /**
     * Gracefully shuts down the rivers.
     */
    public void shutdown() {
        logger.info("System shutdown sequence initiated...");
        running.set(false);
        Optional.ofNullable(connector).ifPresent(SerialConnector::disconnect);
        ntpExecutor.shutdown();
        logger.info("System stopped.");
    }
}
