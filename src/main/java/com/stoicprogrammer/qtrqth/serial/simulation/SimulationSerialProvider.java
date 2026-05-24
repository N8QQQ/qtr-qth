package com.stoicprogrammer.qtrqth.serial.simulation;

import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;

import java.util.List;

/**
 * Provider for Virtual Hardware.
 * Adheres to strict finality and unmodifiable collection mandates.
 */
public final class SimulationSerialProvider implements ISerialProvider {
    private static final int DEFAULT_INTERVAL_MS = 1000;
    private final String dataFile;
    private final int intervalMs;

    public SimulationSerialProvider() {
        this("simulation/gps_sim.nmea", DEFAULT_INTERVAL_MS);
    }

    public SimulationSerialProvider(final String dataFile, final int intervalMs) {
        this.dataFile = dataFile;
        this.intervalMs = intervalMs;
    }

    @Override
    public List<ISerialPort> getAvailablePorts() {
        return List.of(new SimulationSerialPort("SIM1", dataFile, intervalMs));
    }

    @Override
    public ISerialPort getPort(final String portName) {
        return new SimulationSerialPort(portName, dataFile, intervalMs);
    }
}
