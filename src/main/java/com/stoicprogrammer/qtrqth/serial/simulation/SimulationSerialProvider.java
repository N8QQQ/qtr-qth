package com.stoicprogrammer.qtrqth.serial.simulation;

import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;

import java.util.List;

/**
 * Provider for Virtual Hardware.
 * Adheres to strict finality and unmodifiable collection mandates.
 */
public final class SimulationSerialProvider implements ISerialProvider {
    @Override
    public List<ISerialPort> getAvailablePorts() {
        return List.of(new SimulationSerialPort("SIM1"));
    }

    @Override
    public ISerialPort getPort(final String portName) {
        return new SimulationSerialPort(portName);
    }
}
