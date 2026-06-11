package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.serial.jserialcomm.JSerialCommProvider;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Standalone utility for auditing physical and virtual serial hardware.
 * Provides a high-fidelity report of all potential GPS targets.
 */
public final class HardwareProbe {

    private HardwareProbe() {
        // Utility class
    }

    public static void main(final String[] args) {
        System.out.println("--- 📡 qtr-qth: Hardware Discovery Probe ---");
        System.out.println("Scanning system for serial frequencies...");

        final ConfigManager configManager = new ConfigManager(Paths.get("qtr-qth.properties"));
        final ISerialProvider provider = new JSerialCommProvider();
        final PortDiscovery discovery = new PortDiscovery(provider, configManager);

        final List<? extends ISerialPort> ports = provider.getAvailablePorts();

        // Pure Functional Branching
        Map.<Boolean, Consumer<List<? extends ISerialPort>>>of(
            true, p -> {
                System.out.println(String.format("Found %d available port(s):\n", p.size()));
                p.forEach(HardwareProbe::printPortAudit);
                System.out.println("--- 🎯 Discovery Analysis ---");
                discovery.findLikelyGpsPort()
                    .ifPresentOrElse(
                        target -> System.out.println("RECOMMENDED TARGET: " + target),
                        () -> System.out.println("No high-probability GPS targets identified.")
                    );
            },
            false, p -> System.out.println("⚠️ No serial hardware detected.")
        ).get(!ports.isEmpty()).accept(ports);

        System.out.println("-------------------------------------------");
    }

    private static void printPortAudit(final ISerialPort port) {
        System.out.println(String.format("[Port: %s]", port.getSystemPortName()));
        System.out.println(String.format("  Description: %s", port.getPortDescription()));
        System.out.println(String.format("  Metadata:    %s", port.getDescriptivePortName()));
        
        final String speed = AutoBaudEngine.scan(port)
            .map(baud -> baud + " bps (NMEA Detected)")
            .orElse("No Signal / Unknown Speed");
        System.out.println(String.format("  Telemetry:   %s", speed));
        System.out.println("");
    }
}
