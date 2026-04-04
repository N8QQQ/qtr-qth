package com.stoicprogrammer.qtrqth.serial.jserialcomm;

import com.fazecast.jSerialComm.SerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Physical hardware serial provider.
 */
public class JSerialCommProvider implements ISerialProvider {
    @Override
    public List<ISerialPort> getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        List<ISerialPort> list = new ArrayList<>();
        for (SerialPort p : ports) {
            list.add(new JSerialPortWrapper(p));
        }
        return list;
    }

    @Override
    public ISerialPort getPort(String portName) {
        return new JSerialPortWrapper(SerialPort.getCommPort(portName));
    }
}
