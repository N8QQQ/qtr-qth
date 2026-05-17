package com.stoicprogrammer.qtrqth.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import com.stoicprogrammer.qtrqth.util.Functional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Manages the connection to physical or virtual serial hardware using declarative patterns.
 * Adheres to strict finality and expression-based control flow.
 */
public final class SerialConnector {
    private static final Logger logger = LoggerFactory.getLogger(SerialConnector.class);

    // Operational Constants
    private static final int DEFAULT_BAUD = 9600;
    private static final int TELEMETRY_QUEUE_CAPACITY = 100;
    private static final int DATA_BITS = 8;

    private final ConfigManager config;
    private final NmeaSentenceAccumulator accumulator;
    private final ISerialProvider provider;
    private ISerialPort activePort;
    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(TELEMETRY_QUEUE_CAPACITY);

    private record ConnectionRule(boolean condition, Supplier<Stream<String>> action) {}
    private record QueueRule(boolean condition, Runnable action) {}

    public SerialConnector(final ConfigManager config, final NmeaSentenceAccumulator accumulator, final ISerialProvider provider) {
        this.config = config;
        this.accumulator = accumulator;
        this.provider = provider;
    }

    /**
     * Connects to the specified serial port and returns a stream of NMEA sentences.
     * @param portName The system port name (e.g., COM3).
     * @return A Stream of completed NMEA sentences.
     */
    public Stream<String> connect(final String portName) {
        final int baudRate = config.getProperty("serial.baud")
            .flatMap(Functional::tryParseInt)
            .or(() -> {
                logger.warn("Invalid or missing serial.baud in config. Defaulting to {}.", DEFAULT_BAUD);
                return Optional.of(DEFAULT_BAUD);
            })
            .orElse(DEFAULT_BAUD);
        
        logger.debug("Attempting to open port {} at {} baud...", portName, baudRate);
        activePort = provider.getPort(portName);
        activePort.setBaudRate(baudRate);
        activePort.setNumDataBits(DATA_BITS);
        activePort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        activePort.setParity(SerialPort.NO_PARITY);

        final boolean opened = activePort.openPort();

        // Declarative Connection Rule Engine
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

    private Stream<String> startIngestion(final String portName) {
        logger.info("Serial port {} opened successfully.", portName);
        activePort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

            @Override
            public void serialEvent(final SerialPortEvent event) {
                logger.trace("Serial event received: {}", event.getEventType());
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
                            logger.trace("Sentence accumulated: {}", s);
                            final boolean success = queue.offer(s);
                            List.of(
                                new QueueRule(success, () -> {}),
                                new QueueRule(true, () -> logger.warn("Telemetry buffer full. Dropping sentence: {}", s))
                            ).stream()
                             .filter(r -> r.condition)
                             .findFirst()
                             .ifPresent(r -> r.action.run());
                        }));
            }
        });

        // Use takeWhile with a presence check to ensure the stream terminates on interruption.
        return Stream.generate(() -> {
            try { 
                return Optional.ofNullable(queue.take()); 
            } catch (final InterruptedException e) {
                logger.warn("Telemetry stream interrupted.");
                Thread.currentThread().interrupt();
                return Optional.<String>empty();
            }
        }).takeWhile(Optional::isPresent)
          .map(Optional::get);
    }

    private record SerialChunk(byte[] data, int length) {}

    public void disconnect() {
        Optional.ofNullable(activePort)
            .filter(ISerialPort::isOpen)
            .ifPresent(port -> {
                logger.debug("Closing serial port...");
                port.removeDataListener();
                port.closePort();
                logger.info("Serial port closed.");
            });
    }
}
