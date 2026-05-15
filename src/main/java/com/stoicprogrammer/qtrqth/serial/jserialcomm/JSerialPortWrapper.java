package com.stoicprogrammer.qtrqth.serial.jserialcomm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;

/**
 * Physical serial port implementation using the jSerialComm library.
 * Adheres to strict finality mandates.
 */
public final class JSerialPortWrapper implements ISerialPort {
    private final SerialPort port;

    public JSerialPortWrapper(final SerialPort port) {
        this.port = port;
    }

    @Override public boolean openPort() { return port.openPort(); }
    @Override public boolean closePort() { return port.closePort(); }
    @Override public boolean isOpen() { return port.isOpen(); }
    @Override public void setBaudRate(final int baudRate) { port.setBaudRate(baudRate); }
    @Override public void setNumDataBits(final int dataBits) { port.setNumDataBits(dataBits); }
    @Override public void setNumStopBits(final int stopBits) { port.setNumStopBits(stopBits); }
    @Override public void setParity(final int parity) { port.setParity(parity); }
    @Override public int bytesAvailable() { return port.bytesAvailable(); }
    @Override public int readBytes(final byte[] buffer, final int bytesToRead) { return port.readBytes(buffer, bytesToRead); }
    @Override public boolean addDataListener(final SerialPortDataListener listener) { return port.addDataListener(listener); }
    @Override public void removeDataListener() { port.removeDataListener(); }
    @Override public String getSystemPortName() { return port.getSystemPortName(); }
    @Override public String getDescriptivePortName() { return port.getDescriptivePortName(); }
    @Override public String getPortDescription() { return port.getPortDescription(); }
}
