package com.stoicprogrammer.qtrqth.analysis;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.data.Offset;

class StatisticalWindowTest extends BddTest {

    private static final int WINDOW_SIZE_10 = 10;
    private static final int WINDOW_SIZE_2 = 2;
    private static final double TOLERANCE = 0.001;

    // Expected Values
    private static final double EXPECTED_RMS_10_20 = 15811.388;
    private static final double EXPECTED_STABILITY_10_20 = 5000.0;
    private static final double EXPECTED_RMS_20_30 = 25495.097;
    private static final double EXPECTED_STABILITY_20_30 = 5000.0;
    private static final double EXPECTED_RMS_SINGLE_10 = 10000.0;

    // Input Values
    private static final int OFFSET_10_MS = 10;
    private static final int OFFSET_20_MS = 20;
    private static final int OFFSET_30_MS = 30;

    private final WindowFixture fixture = new WindowFixture();

    @Test
    void should_calculate_rms_jitter_correctly() {
        fixture.given_window_capacity(WINDOW_SIZE_10);
        fixture.when_adding_offsets(OFFSET_10_MS, OFFSET_20_MS);
        fixture.then_rms_jitter_is_close_to(EXPECTED_RMS_10_20, TOLERANCE);
    }

    @Test
    void should_calculate_stability_correctly() {
        fixture.given_window_capacity(WINDOW_SIZE_10);
        fixture.when_adding_offsets(OFFSET_10_MS, OFFSET_20_MS);
        fixture.then_stability_is_close_to(EXPECTED_STABILITY_10_20, TOLERANCE);
    }

    @Test
    void should_enforce_max_size_and_maintain_math_integrity() {
        fixture.given_window_capacity(WINDOW_SIZE_2);
        fixture.when_adding_offsets(OFFSET_10_MS, OFFSET_20_MS, OFFSET_30_MS);
        fixture.then_window_size_is(WINDOW_SIZE_2);
        fixture.then_rms_jitter_is_close_to(EXPECTED_RMS_20_30, TOLERANCE);
        fixture.then_stability_is_close_to(EXPECTED_STABILITY_20_30, TOLERANCE);
    }

    @Test
    void should_handle_empty_window() {
        fixture.given_window_capacity(WINDOW_SIZE_10);
        fixture.then_rms_jitter_is_zero();
        fixture.then_stability_is_zero();
    }

    @Test
    void should_handle_single_element_window() {
        fixture.given_window_capacity(WINDOW_SIZE_10);
        fixture.when_adding_offsets(OFFSET_10_MS);
        fixture.then_rms_jitter_is_close_to(EXPECTED_RMS_SINGLE_10, TOLERANCE);
        fixture.then_stability_is_zero();
    }

    private final class WindowFixture {
        private StatisticalWindow window;

        void given_window_capacity(final int capacity) {
            this.window = StatisticalWindow.empty(capacity);
        }

        void when_adding_offsets(final int... offsetsMillis) {
            this.window = Arrays.stream(offsetsMillis)
                .mapToObj(Duration::ofMillis)
                .reduce(this.window, StatisticalWindow::add, (w1, w2) -> w2);
        }

        void then_window_size_is(final int expectedSize) {
            assertThat(window.offsets()).hasSize(expectedSize);
        }

        void then_rms_jitter_is_close_to(final double expected, final double tolerance) {
            assertThat(window.rmsJitterMicroseconds()).isCloseTo(expected, Offset.offset(tolerance));
        }

        void then_stability_is_close_to(final double expected, final double tolerance) {
            assertThat(window.stabilityMicroseconds()).isCloseTo(expected, Offset.offset(tolerance));
        }

        void then_rms_jitter_is_zero() {
            assertThat(window.rmsJitterMicroseconds()).isZero();
        }

        void then_stability_is_zero() {
            assertThat(window.stabilityMicroseconds()).isZero();
        }
    }
}
