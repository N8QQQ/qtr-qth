package com.stoicprogrammer.qtrqth.serial.simulation;

import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Virtual Serial Port that replays NMEA data.
 */
public class SimulationSerialPort implements ISerialPort {
    private final String name;
    private final List<String> script = new ArrayList<>();
    private boolean open = false;
    private SerialPortDataListener listener;
    private Timer timer;
    private int scriptIndex = 0;

    public SimulationSerialPort(String name) {
        this.name = name;
        // Default simulation script (Live capture from VFAN UG-353)
        script.add("$GPRMC,232810.00,A,4617.00579,N,08753.28148,W,0.650,,020426,,,A*68\r\n");
        script.add("$GPGGA,232810.00,4617.00579,N,08753.28148,W,1,07,1.15,431.1,M,-35.0,M,,*6B\r\n");
        script.add("$GPZDA,232810.00,02,04,2026,00,00*6C\r\n");
    }

    @Override
    public boolean openPort() {
        this.open = true;
        this.timer = new Timer(true);
        // Create a dummy real SerialPort for the event source
        com.fazecast.jSerialComm.SerialPort dummy = com.fazecast.jSerialComm.SerialPort.getCommPort(name);
        
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (listener != null) {
                    SerialPortEvent event = new SerialPortEvent(dummy, com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE);
                    listener.serialEvent(event);
                }
            }
        }, 1000, 1000);
        return true;
    }

    @Override
    public boolean closePort() {
        if (timer != null) timer.cancel();
        this.open = false;
        return true;
    }

    @Override
    public boolean isOpen() { return open; }
    @Override public void setBaudRate(int baudRate) {}
    @Override public void setNumDataBits(int dataBits) {}
    @Override public void setNumStopBits(int stopBits) {}
    @Override public void setParity(int parity) {}

    @Override
    public int bytesAvailable() {
        return script.get(scriptIndex).length();
    }

    @Override
    public int readBytes(byte[] buffer, int bytesToRead) {
        String sentence = script.get(scriptIndex);
        byte[] data = sentence.getBytes();
        int count = Math.min(bytesToRead, data.length);
        System.arraycopy(data, 0, buffer, 0, count);
        
        scriptIndex = (scriptIndex + 1) % script.size();
        return count;
    }

    @Override
    public boolean addDataListener(SerialPortDataListener listener) {
        this.listener = listener;
        return true;
    }

    @Override
    public void removeDataListener() { this.listener = null; }
    @Override public String getSystemPortName() { return name; }
    @Override public String getDescriptivePortName() { return "Simulated GPS Device"; }
    @Override public String getPortDescription() { return "JARVIS Simulation Module"; }
}
