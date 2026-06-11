package com.stoicprogrammer.qtrqth.sentinel;

import com.stoicprogrammer.qtrqth.sentinel.api.IStreamSentinel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutorSentinelTest {
    private static final int TARGET_ITERATIONS = 3;
    private static final long INTERVAL_MS = 10;
    private static final long TIMEOUT_MS = 500;

    @Test
    @DisplayName("ExecutorSentinel should execute task periodically")
    void sentinel_should_execute_periodically() throws InterruptedException {
        // Given
        final IStreamSentinel sentinel = new ExecutorSentinel();
        final AtomicInteger counter = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(TARGET_ITERATIONS);

        // When
        sentinel.start(() -> {
            counter.incrementAndGet();
            latch.countDown();
        }, INTERVAL_MS, TimeUnit.MILLISECONDS);

        // Then
        final boolean completed = latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        sentinel.stop();

        assertThat(completed).isTrue();
        assertThat(counter.get()).isGreaterThanOrEqualTo(TARGET_ITERATIONS);
    }
}
