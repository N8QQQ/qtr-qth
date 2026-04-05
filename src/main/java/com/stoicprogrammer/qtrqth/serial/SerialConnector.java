package com.stoicprogrammer.qtrqth.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

/**
 * Manages the connection to physical or virtual serial hardware.
 */
public class SerialConnector {
    private static final Logger logger = LoggerFactory.getLogger(SerialConnector.class);
    private final ConfigManager config;
    private final NmeaSentenceAccumulator accumulator;
    private final ISerialProvider provider;
    private ISerialPort activePort;
    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    public SerialConnector(ConfigManager config, NmeaSentenceAccumulator accumulator, ISerialProvider provider) {
        this.config = config;
        this.accumulator = accumulator;
        this.provider = provider;
    }

    /**
     * Connects to the specified serial port and returns a stream of NMEA sentences.
     * @param portName The system port name (e.g., COM3).
     * @return A Stream of completed NMEA sentences.
     */
    public Stream<String> connect(String portName) {
        int baudRate = Integer.parseInt(config.getProperty("serial.baud"));
        
        logger.debug("Attempting to open port {} at {} baud...", portName, baudRate);
        activePort = provider.getPort(portName);
        activePort.setBaudRate(baudRate);
        activePort.setNumDataBits(8);
        activePort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        activePort.setParity(SerialPort.NO_PARITY);

        if (activePort.openPort()) {
            logger.info("Serial port {} opened successfully.", portName);
            activePort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) return;
                    
                    byte[] newData = new byte[activePort.bytesAvailable()];
                    int numRead = activePort.readBytes(newData, newData.length);
                    
                    for (int i = 0; i < numRead; i++) {
                        accumulator.process(newData[i]).ifPresent(s -> {
                            logger.trace("Sentence accumulated: {}", s);
                            queue.offer(s);
                        });
                    }
                }
            });
            
            return Stream.generate(() -> {
                try {
                    return queue.take();
                } catch (InterruptedException e) {
                    logger.warn("Telemetry stream interrupted.");
                    Thread.currentThread().interrupt();
                    return null;
                }
            }).takeWhile(s -> s != null);
        } else {
            logger.error("Failed to open serial port: {}", portName);
        }
        return Stream.empty();
    }

    public void disconnect() {
        if (activePort != null && activePort.isOpen()) {
            logger.debug("Closing serial port...");
            activePort.removeDataListener();
            activePort.closePort();
            logger.info("Serial port closed.");
        }
    }
}
