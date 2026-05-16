package com.stoicprogrammer.qtrqth.serial.jserialcomm;

import com.fazecast.jSerialComm.SerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;

import java.util.Arrays;
import java.util.List;

/**
 * Physical hardware serial provider.
 * Refactored to use pure Stream-based collection.
 */
public final class JSerialCommProvider implements ISerialProvider {
    @Override
    public List<ISerialPort> getAvailablePorts() {
        return Arrays.stream(SerialPort.getCommPorts())
            .map(JSerialPortWrapper::new)
            .collect(java.util.stream.Collectors.toUnmodifiableList());
    }

    @Override
    public ISerialPort getPort(final String portName) {
        return new JSerialPortWrapper(SerialPort.getCommPort(portName));
    }
}
