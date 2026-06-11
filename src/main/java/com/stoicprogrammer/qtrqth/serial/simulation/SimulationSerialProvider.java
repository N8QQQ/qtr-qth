package com.stoicprogrammer.qtrqth.serial.simulation;

import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;

import java.util.List;

/**
 * Provider for Virtual Hardware.
 * Adheres to strict finality and unmodifiable collection mandates.
 */
public final class SimulationSerialProvider implements ISerialProvider {
    private static final int DEFAULT_INTERVAL_MILLISECONDS = 1000;
    private final String dataFile;
    private final int intervalMilliseconds;

    public SimulationSerialProvider() {
        this("simulation/gps_sim.nmea", DEFAULT_INTERVAL_MILLISECONDS);
    }

    public SimulationSerialProvider(final String dataFile, final int intervalMilliseconds) {
        this.dataFile = dataFile;
        this.intervalMilliseconds = intervalMilliseconds;
    }

    @Override
    public List<ISerialPort> getAvailablePorts() {
        return List.of(new SimulationSerialPort("SIM1", dataFile, intervalMilliseconds));
    }

    @Override
    public ISerialPort getPort(final String portName) {
        return new SimulationSerialPort(portName, dataFile, intervalMilliseconds);
    }
}
