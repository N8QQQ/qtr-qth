package com.stoicprogrammer.qtrqth.analysis;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.data.Offset;

class StatisticalWindowTest {

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

    @Test
    void should_calculate_rms_jitter_correctly() {
        final StatisticalWindow window = StatisticalWindow.empty(WINDOW_SIZE_10)
            .add(Duration.ofMillis(OFFSET_10_MS))
            .add(Duration.ofMillis(OFFSET_20_MS));
        
        assertThat(window.rmsJitterMicroseconds()).isCloseTo(EXPECTED_RMS_10_20, Offset.offset(TOLERANCE));
    }

    @Test
    void should_calculate_stability_correctly() {
        final StatisticalWindow window = StatisticalWindow.empty(WINDOW_SIZE_10)
            .add(Duration.ofMillis(OFFSET_10_MS))
            .add(Duration.ofMillis(OFFSET_20_MS));

        assertThat(window.stabilityMicroseconds()).isCloseTo(EXPECTED_STABILITY_10_20, Offset.offset(TOLERANCE));
    }

    @Test
    void should_enforce_max_size_and_maintain_math_integrity() {
        final StatisticalWindow window = StatisticalWindow.empty(WINDOW_SIZE_2)
            .add(Duration.ofMillis(OFFSET_10_MS))
            .add(Duration.ofMillis(OFFSET_20_MS))
            .add(Duration.ofMillis(OFFSET_30_MS));

        assertThat(window.offsets()).hasSize(WINDOW_SIZE_2);
        
        assertThat(window.rmsJitterMicroseconds()).isCloseTo(EXPECTED_RMS_20_30, Offset.offset(TOLERANCE));
        assertThat(window.stabilityMicroseconds()).isCloseTo(EXPECTED_STABILITY_20_30, Offset.offset(TOLERANCE));
    }

    @Test
    void should_handle_empty_window() {
        final StatisticalWindow window = StatisticalWindow.empty(WINDOW_SIZE_10);
        assertThat(window.rmsJitterMicroseconds()).isZero();
        assertThat(window.stabilityMicroseconds()).isZero();
    }

    @Test
    void should_handle_single_element_window() {
        final StatisticalWindow window = StatisticalWindow.empty(WINDOW_SIZE_10)
            .add(Duration.ofMillis(OFFSET_10_MS));
        
        assertThat(window.rmsJitterMicroseconds()).isCloseTo(EXPECTED_RMS_SINGLE_10, Offset.offset(TOLERANCE));
        assertThat(window.stabilityMicroseconds()).isZero();
    }
}
