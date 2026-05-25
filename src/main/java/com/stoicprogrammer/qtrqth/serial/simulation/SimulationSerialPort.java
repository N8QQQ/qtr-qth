package com.stoicprogrammer.qtrqth.serial.simulation;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Functional simulation of a serial port that generates deterministic GPS data from a resource file.
 * Adheres to strict finality and functional mandates.
 */
public final class SimulationSerialPort implements ISerialPort {
    private static final Logger logger = LoggerFactory.getLogger(SimulationSerialPort.class);
    
    // Operational Constants
    private static final int INITIAL_DELAY_MS = 100;
    private static final int BYTES_AVAILABLE_SIM = 256;

    private final String name;
    private final List<String> dataStream;
    private final int intervalMs;
    private final AtomicInteger lineIndex = new AtomicInteger(0);
    private boolean open = false;
    private SerialPortDataListener listener;
    private Timer timer;

    public SimulationSerialPort(final String name, final String dataFile, final int intervalMs) {
        this.name = name;
        this.dataStream = loadSimulatedData(dataFile);
        this.intervalMs = intervalMs;
    }

    private List<String> loadSimulatedData(final String dataFile) {
        // Try Classpath first, then Absolute Path
        return Optional.ofNullable(getClass().getClassLoader().getResourceAsStream(dataFile))
            .map(is -> Try.of(() -> {
                try (var reader = new BufferedReader(new InputStreamReader(is))) {
                    return reader.lines().toList();
                }
            }).getOrElse(List.<String>of()))
            .or(() -> Try.of(() -> java.nio.file.Files.readAllLines(java.nio.file.Paths.get(dataFile))).toJavaOptional())
            .filter(lines -> !lines.isEmpty())
            .orElseGet(() -> {
                logger.warn("Failed to load simulation data from {}. Falling back to hardcoded baseline.", dataFile);
                return List.of("$GPZDA,232810.00,02,04,2026,00,00*6C");
            });
    }

    @Override public String getSystemPortName() { return name; }
    @Override public String getDescriptivePortName() { return "Deterministic Simulation (" + name + ")"; }
    @Override public String getPortDescription() { return "Simulation"; }
    @Override public void setBaudRate(final int baud) {}
    @Override public void setNumDataBits(final int bits) {}
    @Override public void setNumStopBits(final int stopBits) {}
    @Override public void setParity(final int parity) {}

    @Override
    public boolean openPort() {
        open = true;
        timer = new Timer("sim-gps-timer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final String proxyDescriptor = System.getProperty("os.name").toLowerCase().contains("win") 
                    ? "COM1" 
                    : "/dev/null";
                
                final SerialPort proxy = SerialPort.getCommPort(proxyDescriptor);
                Optional.ofNullable(listener).ifPresent(l -> 
                    l.serialEvent(new SerialPortEvent(proxy, SerialPort.LISTENING_EVENT_DATA_AVAILABLE)));
            }
        }, INITIAL_DELAY_MS, intervalMs);
        return true;
    }

    @Override
    public boolean closePort() {
        open = false;
        Optional.ofNullable(timer).ifPresent(Timer::cancel);
        return true;
    }

    @Override public boolean isOpen() { return open; }
    
    @Override 
    public boolean addDataListener(final SerialPortDataListener l) { 
        this.listener = l; 
        return true; 
    }
    
    @Override public void removeDataListener() { this.listener = null; }
    @Override public int bytesAvailable() { return BYTES_AVAILABLE_SIM; }

    @Override
    public int readBytes(final byte[] buffer, final int bytesToRead) {
        final String raw = dataStream.get(lineIndex.getAndIncrement() % dataStream.size());
        final byte[] data = (raw.trim() + "\r\n").getBytes();
        final int len = Math.min(data.length, bytesToRead);
        System.arraycopy(data, 0, buffer, 0, len);
        return len;
    }
}
