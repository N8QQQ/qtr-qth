package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.config.AppConfig;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * High-fidelity orchestrator for the qtr-qth telemetry hub.
 * Decouples system initialization and pipeline execution from the static Main entry point.
 */
public final class SystemOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(SystemOrchestrator.class);

    private static final int NTP_TIMEOUT_MS = 5000;
    private static final int NTP_POLL_INTERVAL_SECONDS = 60;

    private final ConfigManager configManager;
    private final ScheduledExecutorService ntpExecutor;
    private final AtomicReference<NtpResponse> lastNtp = new AtomicReference<>();
    private final AtomicReference<GpsData> currentFix = new AtomicReference<>(new GpsData(null, null, 0, 0, 0, 0));
    
    private SerialConnector connector;

    public SystemOrchestrator(final Path configPath) {
        this.configManager = new ConfigManager(configPath);
        this.ntpExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "ntp-heartbeat");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Executes the full telemetry confluence pipeline.
     * @param pulseConsumer A consumer for processed pulses (e.g., logging or UI updates).
     */
    public void start(final Consumer<TelemetryPulse> pulseConsumer) {
        final AppConfig config = configManager.getConfig();
        logger.info("System bootstrapping... (Target: {})", config.simulationMode() ? "Simulation" : "Hardware");
        
        // 1. Adaptive Hardware Strategy
        // We attempt to discover physical hardware first. If none is found, we fallback.
        final ISerialProvider physicalProvider = new JSerialCommProvider();
        final PortDiscovery discovery = new PortDiscovery(physicalProvider, configManager);
        final List<String> physicalPorts = discovery.getAvailablePorts();

        final boolean useSimulation = config.simulationMode() || physicalPorts.isEmpty();
        
        if (!config.simulationMode() && physicalPorts.isEmpty()) {
            logger.warn("STRATUM 0 DISCOVERY FAILURE: No physical serial hardware identified.");
            logger.info("ADAPTIVE FALLBACK: Engaging Simulation Mode for functional continuity.");
        }

        final ISerialProvider activeProvider = useSimulation 
            ? new SimulationSerialProvider() 
            : physicalProvider;

        // 2. Network Time Heartbeat
        final INtpProvider ntpProvider = useSimulation
            ? new SimulationNtpProvider()
            : new NetworkNtpProvider();

        final NtpClient ntpClient = new NtpClient(ntpProvider, NTP_TIMEOUT_MS);
        ntpExecutor.scheduleAtFixedRate(() -> 
            ntpClient.pollDetailed(config.ntpPool()).ifPresent(lastNtp::set), 
            0, NTP_POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 3. Final Confluence
        final PortDiscovery activeDiscovery = new PortDiscovery(activeProvider, configManager);
        
        activeDiscovery.findLikelyGpsPort()
            .or(() -> activeDiscovery.getAvailablePorts().stream().findFirst())
            .ifPresentOrElse(
                port -> runPipeline(port, activeProvider, pulseConsumer),
                () -> logger.error("CRITICAL FAILURE: No viable serial paths (Physical or Virtual) identified.")
            );
    }

    private void runPipeline(final String port, final ISerialProvider provider, final Consumer<TelemetryPulse> consumer) {
        final AppConfig config = configManager.getConfig();
        final NmeaParser parser = new NmeaParser();
        this.connector = new SerialConnector(configManager, new NmeaSentenceAccumulator(), provider);

        logger.info("Telemetry Confluence initiated on port {}...", port);

        connector.connect(port)
            .map(sentence -> TelemetryPulse.start(sentence, lastNtp.get()))
            .peek(pulse -> Map.<Boolean, Runnable>of(
                true, () -> pulse.logRaw(logger),
                false, () -> {}
            ).get(config.displayRawTelemetry()).run())
            .map(pulse -> pulse.update(parser, currentFix))
            .filter(TelemetryPulse::hasValidFix)
            .forEach(consumer);
    }

    /**
     * Gracefully shuts down the rivers.
     */
    public void shutdown() {
        logger.info("System shutdown sequence initiated...");
        Optional.ofNullable(connector).ifPresent(SerialConnector::disconnect);
        ntpExecutor.shutdown();
        logger.info("System stopped.");
    }
}
