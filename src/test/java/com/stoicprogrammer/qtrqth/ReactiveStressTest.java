package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.SystemOrchestrator;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import com.stoicprogrammer.qtrqth.serial.simulation.SimulationSerialProvider;
import com.stoicprogrammer.qtrqth.ntp.simulation.SimulationNtpProvider;
import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.util.TelemetryInterpolationEngine;
import com.stoicprogrammer.qtrqth.util.TestArtifactManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * High-Fidelity Stress Test for Phase 9 Reactive Inversion.
 * Verifies the system can handle 25Hz telemetry with zero-latency pulse triggering.
 */
class ReactiveStressTest extends BddTest {
    private static final Logger logger = LoggerFactory.getLogger(ReactiveStressTest.class);
    
    private static final int BASELINE_FREQ = 25;
    private static final int BASELINE_PULSES = 100;
    private static final int HIGH_BANDWIDTH_FREQ = 50;
    private static final int HIGH_BANDWIDTH_PULSES = 200;
    private static final int MILLIS_PER_SEC = 1000;
    private static final int QUEUE_CAPACITY = 10000;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;
    private static final double LATITUDE_EXPECTED = 46.28342983333334;
    private static final double LONGITUDE_EXPECTED = -87.88802466666666;
    private static final double COORDINATE_PRECISION = 0.00000001;
    private static final double MAX_AVG_LAG_MS = 100.0;
    private static final int BILLION_NANOS = 1_000_000_000;
    private static final int HOUR_START = 0;
    private static final int HOUR_END = 2;
    private static final int MINUTE_START = 2;
    private static final int MINUTE_END = 4;
    private static final int SECOND_START = 4;
    private static final int SECOND_END = 6;

    @TempDir
    private Path tempDir;

    @Test
    void should_handle_25hz_telemetry_burst_with_minimal_processing_lag() {
        reportGiven("A 25Hz simulated hardware stream baseline (115,200 baud)");
        runStressTest(BASELINE_FREQ, BASELINE_PULSES);
    }

    @Test
    void should_handle_high_bandwidth_921600_baud_simulation() {
        reportGiven("A high-bandwidth 921,600 baud simulated hardware stream");
        runStressTest(HIGH_BANDWIDTH_FREQ, HIGH_BANDWIDTH_PULSES);
    }

    private void runStressTest(final int targetFrequency, final int burstCount) {
        final Path configPath = tempDir.resolve("stress_" + targetFrequency + ".properties");
        final String stressFile = "comprehensive_stress_" + targetFrequency + ".nmea";
        final Path stressFilePath = tempDir.resolve(stressFile);
        
        final int intervalMs = MILLIS_PER_SEC / targetFrequency;

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
        
        // Secure the artifact using the new manager to prove it is incorruptible
        TestArtifactManager.secureDataset(stressFilePath, stressSentences);
        assertThat(TestArtifactManager.verifyDataset(stressFilePath)).isTrue();
        
        final Properties props = new Properties();
        props.setProperty("simulation.mode", "true");
        props.setProperty("simulation.data.file", stressFilePath.toString());
        props.setProperty("simulation.interval.ms", String.valueOf(intervalMs));
        props.setProperty("telemetry.queue.capacity", String.valueOf(QUEUE_CAPACITY));
        
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

        final AtomicReference<io.vavr.collection.Vector<TelemetryPulse>> capturedPulses = 
            new AtomicReference<>(io.vavr.collection.Vector.empty());
        final AtomicReference<io.vavr.collection.Vector<Long>> processingLags = 
            new AtomicReference<>(io.vavr.collection.Vector.empty());
        final CountDownLatch latch = new CountDownLatch(burstCount);
        
        reportWhen("The orchestrator initiates a " + targetFrequency + "Hz reactive stream (" + stressSentences.size() + " unique sentences)");
        new Thread(() -> orchestrator.start(pulse -> {
            final Instant now = clock.instant();
            final long lag = Duration.between(pulse.ingressTime(), now).toMillis();
            processingLags.updateAndGet(v -> v.append(lag));
            capturedPulses.updateAndGet(v -> v.append(pulse));
            latch.countDown();
        })).start();

        reportThen("The system must capture all " + burstCount + " pulses without data corruption");
        try { 
            final boolean completed = latch.await(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            orchestrator.shutdown();
            assertThat(completed).withFailMessage("Test timed out before capturing all pulses").isTrue();
        } catch (final InterruptedException e) { 
            Thread.currentThread().interrupt(); 
            orchestrator.shutdown();
        }

        assertThat(capturedPulses.get()).hasSizeGreaterThanOrEqualTo(burstCount);
        
        // Deterministic Integrity Verification
        IntStream.range(0, burstCount).forEach(i -> {
            final TelemetryPulse pulse = capturedPulses.get().get(i);
            final String raw = pulse.triggeringSentence();
            final String[] parts = raw.split(",");
            final String expectedRawTime = parts[1];
            
            final int hh = Integer.parseInt(expectedRawTime.substring(HOUR_START, HOUR_END));
            final int mm = Integer.parseInt(expectedRawTime.substring(MINUTE_START, MINUTE_END));
            final int ss = Integer.parseInt(expectedRawTime.substring(SECOND_START, SECOND_END));
            int ns = 0;
            if (expectedRawTime.length() > SECOND_END && expectedRawTime.charAt(SECOND_END) == '.') {
                final double fraction = Double.parseDouble("0" + expectedRawTime.substring(SECOND_END));
                ns = (int) Math.round(fraction * BILLION_NANOS);
            }
            final java.time.LocalTime expectedTime = java.time.LocalTime.of(hh, mm, ss, ns);
            
            assertThat(pulse.data().utcTime()).isEqualTo(expectedTime);
        });
        
        final TelemetryPulse finalPulse = capturedPulses.get().last();
        assertThat(finalPulse.data().latitude()).isCloseTo(LATITUDE_EXPECTED, org.assertj.core.data.Offset.offset(COORDINATE_PRECISION));
        assertThat(finalPulse.data().longitude()).isCloseTo(LONGITUDE_EXPECTED, org.assertj.core.data.Offset.offset(COORDINATE_PRECISION));
        
        final double avgLag = capturedPulses.get().isEmpty() ? 0.0 : processingLags.get().toJavaStream().mapToLong(l -> l).average().orElse(0.0);
        final long maxLag = capturedPulses.get().isEmpty() ? 0L : processingLags.get().toJavaStream().mapToLong(l -> l).max().orElse(0L);
        logger.info("{}Hz Endurance Test Complete. Avg Lag: {}ms | Peak: {}ms | Integrity: Verified", targetFrequency, avgLag, maxLag);
        
        assertThat(avgLag).isLessThan(MAX_AVG_LAG_MS);
    }
}
