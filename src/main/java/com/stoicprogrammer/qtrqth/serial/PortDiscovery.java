package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.config.AppConfig;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;

import java.util.List;
import java.util.Optional;

/**
 * Service for identifying available serial hardware.
 * Adheres to strict finality and functional Stream mandates.
 */
public final class PortDiscovery {

    private final ISerialProvider provider;
    private final AppConfig config;

    public PortDiscovery(final ISerialProvider provider, final ConfigManager configManager) {
        this.provider = provider;
        this.config = configManager.getConfig();
    }

    /**
     * Lists all available serial ports on the system.
     * @return List of system port names.
     */
    public List<String> getAvailablePorts() {
        return provider.getAvailablePorts().stream()
            .map(ISerialPort::getSystemPortName)
            .toList();
    }

    /**
     * Attempts to identify a potential GPS device based on configurable keywords.
     * @return An Optional containing the most likely GPS port name.
     */
    public Optional<String> findLikelyGpsPort() {
        final List<String> keywords = config.gpsDiscoveryKeywords().stream()
            .map(String::toLowerCase)
            .toList();

        return provider.getAvailablePorts().stream()
            .filter(port -> isLikelyGps(port, keywords))
            .map(ISerialPort::getSystemPortName)
            .findFirst();
    }

    private boolean isLikelyGps(final ISerialPort port, final List<String> keywords) {
        final String combinedMetadata = (port.getDescriptivePortName() + " " + port.getPortDescription()).toLowerCase();
        return keywords.stream().anyMatch(combinedMetadata::contains);
    }
}
