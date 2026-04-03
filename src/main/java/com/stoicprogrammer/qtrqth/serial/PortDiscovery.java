package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;

import java.util.ArrayList;
import java.util.List;

public class PortDiscovery {

    private final ISerialProvider provider;
    private final ConfigManager config;

    public PortDiscovery(ISerialProvider provider, ConfigManager config) {
        this.provider = provider;
        this.config = config;
    }

    /**
     * Lists all available serial ports on the system.
     * @return List of system port names.
     */
    public List<String> getAvailablePorts() {
        List<ISerialPort> ports = provider.getAvailablePorts();
        List<String> names = new ArrayList<>();
        for (ISerialPort port : ports) {
            names.add(port.getSystemPortName());
        }
        return names;
    }

    /**
     * Attempts to identify a potential GPS device based on configurable keywords.
     * @return The most likely GPS port name, or null if none found.
     */
    public String findLikelyGpsPort() {
        String[] keywords = config.getProperty("gps.discovery.keywords").split(",");
        List<ISerialPort> ports = provider.getAvailablePorts();
        
        for (ISerialPort port : ports) {
            String name = port.getDescriptivePortName().toLowerCase();
            String desc = port.getPortDescription().toLowerCase();
            
            for (String keyword : keywords) {
                String trimmed = keyword.trim().toLowerCase();
                if (!trimmed.isEmpty() && (name.contains(trimmed) || desc.contains(trimmed))) {
                    return port.getSystemPortName();
                }
            }
        }
        return null;
    }
}


