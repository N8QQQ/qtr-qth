package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.model.TelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/**
 * High-fidelity telemetry recorder for Stratum 0 Audits.
 * Uses an asynchronous buffered writer to prevent disk I/O jitter.
 */
public final class TelemetryCaptor implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TelemetryCaptor.class);
    private static final int QUEUE_CAPACITY = 10000; // Large buffer for high-rate data
    private static final int POLL_TIMEOUT_MILLISECONDS = 100;
    private static final int SHUTDOWN_AWAIT_SECONDS = 5;
    
    private final Path capturePath;
    private final BlockingQueue<TelemetryEvent> writeQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final ExecutorService writerExecutor;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public TelemetryCaptor(final Path capturePath) {
        this.capturePath = capturePath;
        this.writerExecutor = Executors.newSingleThreadExecutor(r -> {
            final Thread t = new Thread(r, "telemetry-captor");
            t.setDaemon(true);
            return t;
        });
        this.writerExecutor.submit(this::processQueue);
    }

    /**
     * Enqueues an event for capture.
     */
    public void capture(final TelemetryEvent event) {
        // Pure Functional Offer
        Map.<Boolean, Runnable>of(
            false, () -> logger.warn("Capture buffer overflow! Dropping telemetry event."),
            true, () -> {}
        ).get(writeQueue.offer(event)).run();
    }

    private void processQueue() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(capturePath.toFile(), true))) {
            logger.info("Telemetry capture initiated at: {}", capturePath.toAbsolutePath());
            
            Stream.generate(() -> pollSafe(POLL_TIMEOUT_MILLISECONDS))
                .takeWhile(eventOpt -> running.get() || !writeQueue.isEmpty())
                .forEach(eventOpt -> eventOpt.ifPresent(event -> safeWriteEvent(writer, event)));
            
            writer.flush();
        } catch (final IOException e) {
            logger.error("Telemetry captor failure: {}", e.getMessage(), e);
        } finally {
            logger.info("Telemetry capture closed.");
        }
    }

    private void safeWriteEvent(final BufferedWriter writer, final TelemetryEvent event) {
        try {
            writeEvent(writer, event);
        } catch (final IOException e) {
            logger.error("Failed to write telemetry event: {}", e.getMessage());
        }
    }

    private Optional<TelemetryEvent> pollSafe(final int timeoutMilliseconds) {
        try {
            return Optional.ofNullable(writeQueue.poll(timeoutMilliseconds, TimeUnit.MILLISECONDS));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private void writeEvent(final BufferedWriter writer, final TelemetryEvent event) throws IOException {
        // Log the system arrival timestamp as a metadata comment for the simulator
        writer.write(String.format("#T1:%d%n", event.ingressTime().toEpochMilli()));
        writer.write(event.rawSentence());
        writer.newLine();
    }

    @Override
    public void close() {
        running.set(false);
        writerExecutor.shutdown();
        
        try {
            // Pure Functional Shutdown
            Map.<Boolean, Runnable>of(
                false, () -> writerExecutor.shutdownNow(),
                true, () -> {}
            ).get(writerExecutor.awaitTermination(SHUTDOWN_AWAIT_SECONDS, TimeUnit.SECONDS)).run();
        } catch (final InterruptedException e) {
            writerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
