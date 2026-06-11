package com.stoicprogrammer.qtrqth.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.model.TelemetryEvent;
import com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import com.stoicprogrammer.qtrqth.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.InstantSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Manages the connection to physical or virtual serial hardware using declarative patterns.
 * Producer Level: Captures Edge Stamps (T1) the instant a sentence is reconstructed.
 */
public final class SerialConnector {
    private static final Logger logger = LoggerFactory.getLogger(SerialConnector.class);

    // Operational Constants
    private static final int DEFAULT_BAUD = 9600;
    private static final int WATCHDOG_TIMEOUT_SECONDS = 5;
    private static final int DATA_BITS = 8;

    private final ConfigManager config;
    private final NmeaSentenceAccumulator accumulator;
    private final ISerialProvider provider;
    private final InstantSource clock;
    private ISerialPort activePort;
    private final LinkedBlockingQueue<TelemetryEvent> queue;

    private record ConnectionRule(boolean condition, Supplier<Stream<TelemetryEvent>> action) {}

    public SerialConnector(final ConfigManager config, final NmeaSentenceAccumulator accumulator, final ISerialProvider provider, final InstantSource clock) {
        this.config = config;
        this.accumulator = accumulator;
        this.provider = provider;
        this.clock = clock;
        this.queue = new LinkedBlockingQueue<>(config.getConfig().telemetryQueueCapacity());
    }

    /**
     * Connects to the specified serial port and returns a stream of high-fidelity TelemetryEvents.
     * @param portName The system port name (e.g., COM3).
     * @return A Stream of TelemetryEvents containing the raw sentence and Edge Stamp.
     */
    public Stream<TelemetryEvent> connect(final String portName) {
        final Optional<Integer> configuredBaud = config.getProperty("serial.baud")
            .flatMap(Functional::tryParseInt);

        activePort = provider.getPort(portName);
        
        // Auto-Baud Logic: If baud is 0, perform an active scan
        final int baudRate = configuredBaud
            .filter(b -> b > 0)
            .or(() -> {
                logger.info("Auto-Baud: Initiating discovery for {}...", portName);
                return AutoBaudEngine.scan(activePort);
            })
            .orElseGet(() -> {
                logger.warn("Auto-Baud: No signal detected. Falling back to {}.", DEFAULT_BAUD);
                return DEFAULT_BAUD;
            });
        
        logger.debug("Opening port {} at {} bps...", portName, baudRate);
        activePort.setBaudRate(baudRate);
        activePort.setNumDataBits(DATA_BITS);
        activePort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        activePort.setParity(SerialPort.NO_PARITY);

        final boolean opened = activePort.openPort();

        return List.of(
            new ConnectionRule(opened, () -> startIngestion(portName)),
            new ConnectionRule(true, () -> {
                logger.error("Failed to open serial port: {}", portName);
                return Stream.empty();
            })
        ).stream()
         .filter(r -> r.condition)
         .findFirst()
         .map(r -> r.action.get())
         .orElse(Stream.empty());
    }

    private Stream<TelemetryEvent> startIngestion(final String portName) {
        logger.info("Serial port {} opened successfully.", portName);
        activePort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

            @Override
            public void serialEvent(final SerialPortEvent event) {
                Optional.of(event)
                    .filter(e -> e.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                    .map(e -> {
                        final byte[] buf = new byte[activePort.bytesAvailable()];
                        final int read = activePort.readBytes(buf, buf.length);
                        return new SerialChunk(buf, read);
                    })
                    .ifPresent(chunk -> IntStream.range(0, chunk.length)
                        .mapToObj(i -> chunk.data[i])
                        .map(accumulator::process)
                        .flatMap(Optional::stream)
                        .forEach(s -> {
                            // T1: Edge Stamp captured immediately upon line reconstruction
                            final TelemetryEvent te = new TelemetryEvent(s, clock.instant());
                            
                            // Pure Functional Queue Handling
                            Map.<Boolean, Runnable>of(
                                false, () -> logger.warn("Telemetry buffer full. Dropping sentence: {}", s),
                                true, () -> {}
                            ).get(queue.offer(te)).run();
                        }));
            }
        });

        return Stream.generate(() -> {
            try { 
                return Optional.ofNullable(queue.poll(WATCHDOG_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            } catch (final InterruptedException e) {
                logger.warn("Telemetry stream interrupted.");
                Thread.currentThread().interrupt();
                return Optional.<TelemetryEvent>empty();
            }
        }).takeWhile(Optional::isPresent)
          .map(Optional::get)
          .takeWhile(e -> !e.isSignalLoss());
    }

    private record SerialChunk(byte[] data, int length) {}

    public void disconnect() {
        Optional.ofNullable(activePort)
            .ifPresent(port -> {
                logger.debug("Neutralizing serial port handle...");
                port.removeDataListener();
                port.closePort();
                logger.info("Serial port neutralized.");
            });
    }
}
