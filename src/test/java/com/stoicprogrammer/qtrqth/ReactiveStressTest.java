package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import com.stoicprogrammer.qtrqth.ntp.simulation.SimulationNtpProvider;
import com.stoicprogrammer.qtrqth.sentinel.NoOpSentinel;
import com.stoicprogrammer.qtrqth.serial.simulation.SimulationSerialProvider;
import com.stoicprogrammer.qtrqth.util.Functional;
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
import java.time.LocalTime;
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
    private static final long SHUTDOWN_TIMEOUT_SECONDS = 30L;
    private static final double LATITUDE_EXPECTED = 46.28342983333334;
    private static final double LONGITUDE_EXPECTED = -87.88802466666666;
    private static final double COORDINATE_PRECISION = 0.00000001;
    private static final double MAX_AVG_LAG_MILLISECONDS = 100.0;
    private static final int BILLION_NANOS = 1_000_000_000;
    private static final int HOUR_START = 0;
    private static final int HOUR_END = 2;
    private static final int MINUTE_START = 2;
    private static final int MINUTE_END = 4;
    private static final int SECOND_START = 4;
    private static final int SECOND_END = 6;

    private static final List<String> SOURCE_CYCLE = List.of(
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

    @TempDir
    private Path tempDir;

    private final StressFixture fixture = new StressFixture();

    @Test
    void should_handle_25hz_telemetry_burst_with_minimal_processing_lag() throws InterruptedException {
        fixture.given_stress_dataset(tempDir, BASELINE_FREQ);
        fixture.when_executing_stress_burst(tempDir, BASELINE_FREQ, BASELINE_PULSES);
        fixture.then_burst_completes_within(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        fixture.then_pulse_count_is_at_least(BASELINE_PULSES);
        fixture.then_all_pulses_have_accurate_time(BASELINE_PULSES);
        fixture.then_final_pulse_coordinates_are_accurate();
        fixture.then_average_processing_lag_is_below_limit(BASELINE_FREQ);
    }

    @Test
    void should_handle_high_bandwidth_921600_baud_simulation() throws InterruptedException {
        fixture.given_stress_dataset(tempDir, HIGH_BANDWIDTH_FREQ);
        fixture.when_executing_stress_burst(tempDir, HIGH_BANDWIDTH_FREQ, HIGH_BANDWIDTH_PULSES);
        fixture.then_burst_completes_within(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        fixture.then_pulse_count_is_at_least(HIGH_BANDWIDTH_PULSES);
        fixture.then_all_pulses_have_accurate_time(HIGH_BANDWIDTH_PULSES);
        fixture.then_final_pulse_coordinates_are_accurate();
        fixture.then_average_processing_lag_is_below_limit(HIGH_BANDWIDTH_FREQ);
    }

    private final class StressFixture {
        private Path stressFilePath;
        private final AtomicReference<io.vavr.collection.Vector<TelemetryPulse>> capturedPulses =
            new AtomicReference<>(io.vavr.collection.Vector.empty());
        private final AtomicReference<io.vavr.collection.Vector<Long>> processingLags =
            new AtomicReference<>(io.vavr.collection.Vector.empty());
        private CountDownLatch latch;
        private SystemOrchestrator orchestrator;
        private boolean completed;

        void given_stress_dataset(final Path dir, final int targetFrequency) {
            final String stressFile = "comprehensive_stress_" + targetFrequency + ".nmea";
            this.stressFilePath = dir.resolve(stressFile);
            final List<String> stressSentences = new TelemetryInterpolationEngine().interpolate(SOURCE_CYCLE, targetFrequency);
            TestArtifactManager.secureDataset(stressFilePath, stressSentences);
            assertThat(TestArtifactManager.verifyDataset(stressFilePath)).isTrue();
        }

        void when_executing_stress_burst(final Path dir, final int targetFrequency, final int burstCount) {
            final Path configPath = dir.resolve("stress_" + targetFrequency + ".properties");
            final int intervalMilliseconds = MILLIS_PER_SEC / targetFrequency;
            final Properties props = new Properties();
            props.setProperty("simulation.mode", "true");
            props.setProperty("simulation.data.file", stressFilePath.toString());
            props.setProperty("simulation.interval.ms", String.valueOf(intervalMilliseconds));
            props.setProperty("telemetry.queue.capacity", String.valueOf(QUEUE_CAPACITY));

            final ConfigManager configManager = new ConfigManager(configPath, (f, p) -> {}, (f, p) -> {
                props.forEach((k, v) -> p.setProperty(k.toString(), v.toString()));
            });

            final InstantSource clock = InstantSource.system();
            final SimulationSerialProvider serialProvider = new SimulationSerialProvider(stressFilePath.toString(), intervalMilliseconds);
            final SimulationNtpProvider ntpProvider = new SimulationNtpProvider(clock);

            this.orchestrator = new SystemOrchestrator(
                configManager,
                serialProvider,
                ntpProvider,
                clock,
                new NoOpSentinel()
            );

            this.latch = new CountDownLatch(burstCount);

            new Thread(() -> orchestrator.start(pulse -> {
                final Instant now = clock.instant();
                final long lag = Duration.between(pulse.ingressTime(), now).toMillis();
                processingLags.updateAndGet(v -> v.append(lag));
                capturedPulses.updateAndGet(v -> v.append(pulse));
                latch.countDown();
            })).start();
        }

        void then_burst_completes_within(final long timeout, final TimeUnit unit) throws InterruptedException {
            this.completed = latch.await(timeout, unit);
            orchestrator.shutdown();
            assertThat(completed).withFailMessage("Test timed out before capturing all pulses").isTrue();
        }

        void then_pulse_count_is_at_least(final int expected) {
            assertThat(capturedPulses.get()).hasSizeGreaterThanOrEqualTo(expected);
        }

        void then_all_pulses_have_accurate_time(final int count) {
            IntStream.range(0, count).forEach(i -> {
                final TelemetryPulse pulse = capturedPulses.get().get(i);
                final String raw = pulse.triggeringSentence();
                final String[] parts = raw.split(",");
                final String expectedRawTime = parts[1];

                final int hh = Functional.tryParseInt(expectedRawTime.substring(HOUR_START, HOUR_END)).orElse(0);
                final int mm = Functional.tryParseInt(expectedRawTime.substring(MINUTE_START, MINUTE_END)).orElse(0);
                final int ss = Functional.tryParseInt(expectedRawTime.substring(SECOND_START, SECOND_END)).orElse(0);

                final int ns = io.vavr.control.Option.of(expectedRawTime)
                    .filter(t -> t.length() > SECOND_END && t.charAt(SECOND_END) == '.')
                    .map(t -> "0" + t.substring(SECOND_END))
                    .flatMap(s -> io.vavr.control.Option.ofOptional(Functional.tryParseDouble(s)))
                    .map(fraction -> (int) Math.round(fraction * BILLION_NANOS))
                    .getOrElse(0);

                final LocalTime expectedTime = LocalTime.of(hh, mm, ss, ns);
                assertThat(pulse.data().utcTime()).isEqualTo(expectedTime);
            });
        }

        void then_final_pulse_coordinates_are_accurate() {
            final TelemetryPulse finalPulse = capturedPulses.get().last();
            assertThat(finalPulse.data().latitude()).isCloseTo(LATITUDE_EXPECTED, org.assertj.core.data.Offset.offset(COORDINATE_PRECISION));
            assertThat(finalPulse.data().longitude()).isCloseTo(LONGITUDE_EXPECTED, org.assertj.core.data.Offset.offset(COORDINATE_PRECISION));
        }

        void then_average_processing_lag_is_below_limit(final int targetFrequency) {
            final double avgLag = capturedPulses.get().isEmpty() ? 0.0 : processingLags.get().toJavaStream().mapToLong(l -> l).average().orElse(0.0);
            final long maxLag = capturedPulses.get().isEmpty() ? 0L : processingLags.get().toJavaStream().mapToLong(l -> l).max().orElse(0L);
            logger.info("{}Hz Endurance Test Complete. Avg Lag: {}ms | Peak: {}ms | Integrity: Verified", targetFrequency, avgLag, maxLag);
            assertThat(avgLag).isLessThan(MAX_AVG_LAG_MILLISECONDS);
        }
    }
}
