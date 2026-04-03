package com.stoicprogrammer.qtrqth.serial.api;

import com.fazecast.jSerialComm.SerialPortDataListener;

/**
 * Abstraction for a Physical or Virtual Serial Port.
 */
public interface ISerialPort {
    boolean openPort();
    boolean closePort();
    boolean isOpen();
    void setBaudRate(int baudRate);
    void setNumDataBits(int dataBits);
    void setNumStopBits(int stopBits);
    void setParity(int parity);
    
    int bytesAvailable();
    int readBytes(byte[] buffer, int bytesToRead);
    
    boolean addDataListener(SerialPortDataListener listener);
    void removeDataListener();
    
    String getSystemPortName();
    String getDescriptivePortName();
    String getPortDescription();
}
