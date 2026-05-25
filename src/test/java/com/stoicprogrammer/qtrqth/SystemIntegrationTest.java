package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.serial.SerialConnector;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import com.stoicprogrammer.qtrqth.serial.simulation.SimulationSerialProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.InstantSource;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * High-fidelity integration test for the reactive confluence pipeline.
 * Uses the deterministic SimulationSerialProvider and Deterministic Monolith patterns.
 */
class SystemIntegrationTest extends BddTest {

    private static final int MOCK_LIMIT = 10;
    private static final int JOIN_TIMEOUT_MS = 10000;

    @TempDir
    private Path tempDir;

    @Test
    void should_trigger_pulses_from_simulated_serial_stream() throws Exception {
        // GIVEN: A real simulation provider (deterministic)
        final ISerialProvider simProvider = new SimulationSerialProvider();
        final ConfigManager configManager = new ConfigManager(tempDir.resolve("test.properties"));
        final InstantSource clock = InstantSource.system();
        final SerialConnector connector = new SerialConnector(configManager, new com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator(), simProvider, clock);
        final NmeaParser parser = new NmeaParser();
        final List<TelemetryPulse> capturedPulses = new CopyOnWriteArrayList<>();
        final AtomicReference<GpsData> state = new AtomicReference<>(GpsData.EMPTY);

        // WHEN: The stream is connected and processed in the Deterministic Monolith loop
        final Thread testThread = new Thread(() -> {
            connector.connect("SIM1")
                .limit(MOCK_LIMIT) 
                .forEach(event -> {
                    final String sentence = event.rawSentence();
                    state.updateAndGet(s -> parser.parse(sentence, s));
                    if (parser.isTrigger(sentence)) {
                        capturedPulses.add(TelemetryPulse.start(
                            sentence, 
                            null, 
                            com.stoicprogrammer.qtrqth.model.ConfluenceHealth.HEALTHY_HARDWARE, 
                            event.ingressTime(), // Carry the producer stamp
                            state.get()
                        ));
                    }
                });
        });
        testThread.start();
        testThread.join(JOIN_TIMEOUT_MS); 

        // THEN: We should have captured pulses
        assertThat(capturedPulses).as("Should have captured pulses from simulation").isNotEmpty();
        assertThat(capturedPulses.get(0).hasValidFix()).isTrue();
        
        connector.disconnect();
    }
}
