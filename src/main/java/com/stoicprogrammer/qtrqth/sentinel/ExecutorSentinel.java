package com.stoicprogrammer.qtrqth.sentinel;

import com.stoicprogrammer.qtrqth.sentinel.api.IStreamSentinel;
import io.vavr.control.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Standard implementation of the IStreamSentinel using a ScheduledExecutorService.
 * Suitable for high-performance workstations (Linux/Windows).
 */
public final class ExecutorSentinel implements IStreamSentinel {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorSentinel.class);

    private final ScheduledExecutorService executor;
    private final AtomicReference<ScheduledFuture<?>> currentTask = new AtomicReference<>();

    public ExecutorSentinel() {
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "sentinel-thread");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void start(final Runnable task, final long interval, final TimeUnit unit) {
        Option.of(currentTask.get())
            .peek(f -> f.cancel(false));

        final ScheduledFuture<?> future = executor.scheduleAtFixedRate(
            task, 
            0, 
            interval, 
            unit
        );
        currentTask.set(future);
        logger.debug("Sentinel started with interval {} {}", interval, unit);
    }

    @Override
    public void stop() {
        logger.debug("Sentinel stopping...");
        Option.of(currentTask.get())
            .peek(f -> f.cancel(false));
        executor.shutdown();
        logger.debug("Sentinel stopped.");
    }
}
