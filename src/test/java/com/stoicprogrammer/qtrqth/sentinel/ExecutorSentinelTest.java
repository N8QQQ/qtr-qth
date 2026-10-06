package com.stoicprogrammer.qtrqth.sentinel;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.sentinel.api.IStreamSentinel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutorSentinelTest extends BddTest {
    private static final int TARGET_ITERATIONS = 3;
    private static final long INTERVAL_MS = 10L;
    private static final long TIMEOUT_SECONDS = 3L;

    private final SentinelFixture fixture = new SentinelFixture();

    @Test
    @DisplayName("ExecutorSentinel should execute task periodically")
    void sentinel_should_execute_periodically() throws InterruptedException {
        fixture.given_sentinel_with_latch(TARGET_ITERATIONS);
        fixture.when_starting_periodic_task(INTERVAL_MS, TimeUnit.MILLISECONDS);
        fixture.then_latch_completes_within(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        fixture.then_counter_is_at_least(TARGET_ITERATIONS);
    }

    private final class SentinelFixture {
        private final IStreamSentinel sentinel = new ExecutorSentinel();
        private final AtomicInteger counter = new AtomicInteger(0);
        private CountDownLatch latch;
        private boolean completed;

        void given_sentinel_with_latch(final int count) {
            this.latch = new CountDownLatch(count);
        }

        void when_starting_periodic_task(final long interval, final TimeUnit unit) {
            sentinel.start(() -> {
                counter.incrementAndGet();
                latch.countDown();
            }, interval, unit);
        }

        void then_latch_completes_within(final long timeout, final TimeUnit unit) throws InterruptedException {
            this.completed = latch.await(timeout, unit);
            sentinel.stop();
            assertThat(completed)
                .as("Timed out waiting for Sentinel task execution within %d %s", timeout, unit)
                .isTrue();
        }

        void then_counter_is_at_least(final int expected) {
            assertThat(counter.get()).isGreaterThanOrEqualTo(expected);
        }
    }
}
