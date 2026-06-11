package com.stoicprogrammer.qtrqth.sentinel;

import com.stoicprogrammer.qtrqth.sentinel.api.IStreamSentinel;

import java.util.concurrent.TimeUnit;

/**
 * High-fidelity sentinel that does nothing.
 * Used for testing environments where threads or side-effects are undesirable.
 */
public final class NoOpSentinel implements IStreamSentinel {
    @Override
    public void start(final Runnable task, final long interval, final TimeUnit unit) {
        // No-Op
    }

    @Override
    public void stop() {
        // No-Op
    }
}
