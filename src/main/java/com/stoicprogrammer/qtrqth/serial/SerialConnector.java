package com.stoicprogrammer.qtrqth.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.nmea.NmeaSentenceAccumulator;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;

import java.util.function.Consumer;

public class SerialConnector {
    private final ConfigManager config;
    private final NmeaSentenceAccumulator accumulator;
    private final ISerialProvider provider;
    private ISerialPort activePort;

    public SerialConnector(ConfigManager config, NmeaSentenceAccumulator accumulator, ISerialProvider provider) {
        this.config = config;
        this.accumulator = accumulator;
        this.provider = provider;
    }

    /**
     * Connects to the specified serial port and starts listening for NMEA sentences.
     * @param portName The system port name (e.g., COM3).
     * @param sentenceHandler Callback function for completed NMEA sentences.
     * @return true if connection was successful, false otherwise.
     */
    public boolean connect(String portName, Consumer<String> sentenceHandler) {
        int baudRate = Integer.parseInt(config.getProperty("serial.baud"));
        
        activePort = provider.getPort(portName);
        activePort.setBaudRate(baudRate);
        activePort.setNumDataBits(8);
        activePort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        activePort.setParity(SerialPort.NO_PARITY);

        if (activePort.openPort()) {
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
                        String sentence = accumulator.addByte(newData[i]);
                        if (sentence != null) {
                            sentenceHandler.accept(sentence);
                        }
                    }
                }
            });
            return true;
        }
        return false;
    }

    public void disconnect() {
        if (activePort != null && activePort.isOpen()) {
            activePort.removeDataListener();
            activePort.closePort();
        }
    }
}

