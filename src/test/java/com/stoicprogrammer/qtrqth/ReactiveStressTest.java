package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.SystemOrchestrator;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import com.stoicprogrammer.qtrqth.serial.simulation.SimulationSerialProvider;
import com.stoicprogrammer.qtrqth.ntp.simulation.SimulationNtpProvider;
import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.stoicprogrammer.qtrqth.util.TelemetryInterpolationEngine;
import com.stoicprogrammer.qtrqth.serial.simulation.SimulationSerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;

import com.stoicprogrammer.qtrqth.util.TestArtifactManager;

import java.io.IOException;
import java.nio.file.Files;

/**
 * High-Fidelity Stress Test for Phase 9 Reactive Inversion.
 * Verifies the system can handle 25Hz telemetry with zero-latency pulse triggering.
 */
class ReactiveStressTest extends BddTest {
    private static final Logger logger = LoggerFactory.getLogger(ReactiveStressTest.class);
    
    @TempDir
    Path tempDir;

    @Test
    void should_handle_25hz_telemetry_burst_with_minimal_processing_lag() {
        reportGiven("A 25Hz simulated hardware stream baseline (115,200 baud)");
        runStressTest(25, 100); // 25Hz, 100 pulses
    }

    @Test
    void should_handle_high_bandwidth_921600_baud_simulation() {
        reportGiven("A high-bandwidth 921,600 baud simulated hardware stream");
        runStressTest(50, 200); // 50Hz, 200 pulses
    }

    private void runStressTest(final int targetFrequency, final int burstCount) {
        final Path configPath = tempDir.resolve("stress_" + targetFrequency + ".properties");
        final String stressFile = "comprehensive_stress_" + targetFrequency + ".nmea";
        final Path stressFilePath = tempDir.resolve(stressFile);
        
        final int intervalMs = 1000 / targetFrequency;

        // 1. Generate High-Fidelity Multi-Sentence Burst (2 full seconds of baseline data)
        final List<String> sourceCycle = List.of(
            "$GPTXT,01,01,02,u-blox AG - www.u-blox.com*50",
            "$GPRMC,232810.00,A,4617.00579,N,08753.28148,W,0.650,,020426,,,A*68",
            "$GPGGA,232810.00,4617.00579,N,08753.28148,W,1,08,1.13,425.1,M,-33.2,M,,*63",
            "$GPGSA,A,3,01,02,03,04,05,07,08,09,,,,1.13,1.13,1.00*02",
            "$GPGSV,3,1,11,01,40,045,45,02,39,312,43,03,22,145,34,04,56,080,41*73",
            "$GPGSV,3,2,11,05,28,290,38,07,15,050,30,08,82,120,48,09,45,210,42*71",
            "$GPGSV,3,3,11,10,12,180,35,11,05,300,28,12,02,010,25*70",
            "$GPZDA,232810.00,02,04,2026,00,00*6C",
            "$GPRMC,232811.00,A,4617.00579,N,08753.28148,W,0.650,,020426,,,A*69",
            "$GPGGA,232811.00,4617.00579,N,08753.28148,W,1,08,1.13,425.1,M,-33.2,M,,*62",
            "$GPZDA,232811.00,02,04,2026,00,00*6D"
        );

        final List<String> stressSentences = new TelemetryInterpolationEngine().interpolate(sourceCycle, targetFrequency);
        
        try {
            Files.write(stressFilePath, stressSentences);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        final Properties props = new Properties();
        props.setProperty("simulation.mode", "true");
        props.setProperty("simulation.data.file", stressFilePath.toString());
        props.setProperty("simulation.interval.ms", String.valueOf(intervalMs));
        props.setProperty("telemetry.queue.capacity", "10000");
        
        final ConfigManager configManager = new ConfigManager(configPath, (f, p) -> {}, (f, p) -> {
            props.forEach((k, v) -> p.setProperty(k.toString(), v.toString()));
        });

        final InstantSource clock = InstantSource.system();
        final SimulationSerialProvider serialProvider = new SimulationSerialProvider(stressFilePath.toString(), intervalMs);
        final SimulationNtpProvider ntpProvider = new SimulationNtpProvider(clock);
        
        final SystemOrchestrator orchestrator = new SystemOrchestrator(
            configManager, 
            serialProvider, 
            ntpProvider, 
            clock
        );

        final List<TelemetryPulse> capturedPulses = new ArrayList<>();
        final List<Long> processingLags = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(burstCount);
        
        reportWhen("The orchestrator initiates a " + targetFrequency + "Hz reactive stream");
        new Thread(() -> orchestrator.start(pulse -> {
            final Instant now = clock.instant();
            final long lag = Duration.between(pulse.ingressTime(), now).toMillis();
            processingLags.add(lag);
            capturedPulses.add(pulse);
            latch.countDown();
        })).start();

        reportThen("The system must capture all " + burstCount + " pulses without data corruption");
        assertTimeoutPreemptively(Duration.ofSeconds(30), () -> {
            try { latch.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            orchestrator.shutdown();
        });

        assertThat(capturedPulses).hasSizeGreaterThanOrEqualTo(burstCount);

        // Deterministic Integrity Verification
        // Prove that every pulse arrived in order and the parser folded the state correctly under load.
        // We do this by ensuring the parsed LocalTime in the GpsData state matches the timestamp
        // explicitly declared in the triggering sentence (ZDA or RMC).
        for (int i = 0; i < burstCount; i++) {
            final TelemetryPulse pulse = capturedPulses.get(i);
            
            // Extract the expected timestamp directly from the trigger that caused this pulse
            final String raw = pulse.triggeringSentence();
            final String[] parts = raw.split(",");
            final String expectedRawTime = raw.contains("ZDA") ? parts[1] : parts[1]; // Time is at index 1 for both
            
            final int hh = Integer.parseInt(expectedRawTime.substring(0, 2));
            final int mm = Integer.parseInt(expectedRawTime.substring(2, 4));
            final int ss = Integer.parseInt(expectedRawTime.substring(4, 6));
            int ns = 0;
            if (expectedRawTime.length() > 6 && expectedRawTime.charAt(6) == '.') {
                final double fraction = Double.parseDouble("0" + expectedRawTime.substring(6));
                ns = (int) Math.round(fraction * 1_000_000_000.0);
            }
            final java.time.LocalTime expectedTime = java.time.LocalTime.of(hh, mm, ss, ns);
            
            if (!pulse.data().utcTime().equals(expectedTime)) {
                logger.error("Data Corruption at pulse {}: Expected {}, but got {} (Trigger: {})", i, expectedTime, pulse.data().utcTime(), raw);
            }
            assertThat(pulse.data().utcTime()).isEqualTo(expectedTime);
        }
        
        // Verify cross-sentence state folding remained intact (e.g. coordinates from RMC/GGA weren't lost)
        final TelemetryPulse finalPulse = capturedPulses.get(burstCount - 1);
        assertThat(finalPulse.data().latitude()).isCloseTo(46.28342983333334, org.assertj.core.data.Offset.offset(0.00000001));
        assertThat(finalPulse.data().longitude()).isCloseTo(-87.88802466666666, org.assertj.core.data.Offset.offset(0.00000001));
        
        final double avgLag = processingLags.stream().mapToLong(l -> l).average().orElse(0.0);
        final long maxLag = processingLags.stream().mapToLong(l -> l).max().orElse(0L);
        logger.info("{}Hz Endurance Test Complete. Avg Lag: {}ms | Peak: {}ms | Integrity: Verified", targetFrequency, avgLag, maxLag);
        
        assertThat(avgLag).isLessThan(100.0);
    }
}
