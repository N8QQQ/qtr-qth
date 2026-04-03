package com.stoicprogrammer.qtrqth.serial.api;

import java.util.List;

/**
 * Strategy for discovering and creating Serial Ports.
 */
public interface ISerialProvider {
    List<ISerialPort> getAvailablePorts();
    ISerialPort getPort(String portName);
}
