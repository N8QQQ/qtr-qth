package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.model.TelemetryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * High-fidelity telemetry recorder for Stratum 0 Audits.
 * Uses an asynchronous buffered writer to prevent disk I/O jitter.
 */
public final class TelemetryCaptor implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TelemetryCaptor.class);
    private static final int QUEUE_CAPACITY = 10000; // Large buffer for high-rate data
    
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
        if (!writeQueue.offer(event)) {
            logger.warn("Capture buffer overflow! Dropping telemetry event.");
        }
    }

    private void processQueue() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(capturePath.toFile(), true))) {
            logger.info("Telemetry capture initiated at: {}", capturePath.toAbsolutePath());
            
            while (running.get() || !writeQueue.isEmpty()) {
                final TelemetryEvent event = writeQueue.poll(100, TimeUnit.MILLISECONDS);
                if (event != null) {
                    writeEvent(writer, event);
                }
            }
            writer.flush();
        } catch (final IOException | InterruptedException e) {
            logger.error("Telemetry captor failure: {}", e.getMessage(), e);
        } finally {
            logger.info("Telemetry capture closed.");
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
            if (!writerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                writerExecutor.shutdownNow();
            }
        } catch (final InterruptedException e) {
            writerExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
