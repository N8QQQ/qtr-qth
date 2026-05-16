package com.stoicprogrammer.qtrqth.serial.simulation;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;

import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Functional simulation of a serial port that generates synthetic GPS data.
 * Adheres to strict finality and unmodifiable collection mandates.
 */
public final class SimulationSerialPort implements ISerialPort {
    private final String name;
    private boolean open = false;
    private SerialPortDataListener listener;
    private Timer timer;

    public SimulationSerialPort(final String name) {
        this.name = name;
    }

    @Override public String getSystemPortName() { return name; }
    @Override public String getDescriptivePortName() { return "Simulated GPS Device (" + name + ")"; }
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
                Optional.ofNullable(listener).ifPresent(l -> l.serialEvent(new SerialPortEvent(null, SerialPort.LISTENING_EVENT_DATA_AVAILABLE)));
            }
        }, 1000, 1000);
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
    @Override public int bytesAvailable() { return 100; }

    @Override
    public int readBytes(final byte[] buffer, final int bytesToRead) {
        // Generate a synthetic GPZDA sentence: $GPZDA,hhmmss.ss,dd,mm,yyyy,xx,yy*CC
        final String raw = "$GPZDA,232810.00,02,04,2026,00,00*6C\r\n";
        final byte[] data = raw.getBytes();
        final int len = Math.min(data.length, bytesToRead);
        System.arraycopy(data, 0, buffer, 0, len);
        return len;
    }
}
