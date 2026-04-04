package com.stoicprogrammer.qtrqth.serial.jserialcomm;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;

/**
 * Physical serial port implementation using the jSerialComm library.
 */
public class JSerialPortWrapper implements ISerialPort {
    private final SerialPort port;

    public JSerialPortWrapper(SerialPort port) {
        this.port = port;
    }

    @Override
    public boolean openPort() { return port.openPort(); }
    @Override
    public boolean closePort() { return port.closePort(); }
    @Override
    public boolean isOpen() { return port.isOpen(); }
    @Override
    public void setBaudRate(int baudRate) { port.setBaudRate(baudRate); }
    @Override
    public void setNumDataBits(int dataBits) { port.setNumDataBits(dataBits); }
    @Override
    public void setNumStopBits(int stopBits) { port.setNumStopBits(stopBits); }
    @Override
    public void setParity(int parity) { port.setParity(parity); }
    @Override
    public int bytesAvailable() { return port.bytesAvailable(); }
    @Override
    public int readBytes(byte[] buffer, int bytesToRead) { return port.readBytes(buffer, bytesToRead); }
    @Override
    public boolean addDataListener(SerialPortDataListener listener) { return port.addDataListener(listener); }
    @Override
    public void removeDataListener() { port.removeDataListener(); }
    @Override
    public String getSystemPortName() { return port.getSystemPortName(); }
    @Override
    public String getDescriptivePortName() { return port.getDescriptivePortName(); }
    @Override
    public String getPortDescription() { return port.getPortDescription(); }
}
