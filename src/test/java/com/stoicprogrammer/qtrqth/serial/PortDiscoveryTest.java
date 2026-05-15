package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;

/**
 * Business Rule: [PHASE 2, STEP 2] - Serial Port Discovery.
 */
class PortDiscoveryTest extends BddTest {

    private final DiscoveryFixture fixture = new DiscoveryFixture();

    @Test
    void givenHardwareWithGps_whenScanning_thenGpsPortIsIdentified() {
        fixture.givenMockHardwareHasPort("COM5", "u-blox 7 - GPS/GNSS Receiver");
        fixture.whenScanning();
        fixture.thenLikelyGpsIs("COM5");
    }

    @Test
    void givenGenericHardware_whenScanning_thenNoLikelyGpsFound() {
        fixture.givenMockHardwareHasPort("COM1", "Generic Communications Port");
        fixture.whenScanning();
        fixture.thenNoLikelyGpsFound();
    }

    @Test
    void givenHardwareWithMultiplePorts_whenListing_thenAllPortNamesAreReturned() {
        fixture.givenMockHardwareHasPorts(List.of("COM1", "COM2"));
        fixture.whenListingPorts();
        fixture.thenPortListContains("COM1", "COM2");
    }

    private class DiscoveryFixture {
        private final ISerialProvider mockProvider = mock(ISerialProvider.class);
        private final ConfigManager mockConfig = mock(ConfigManager.class);
        private final PortDiscovery discovery = new PortDiscovery(mockProvider, mockConfig);
        private Optional<String> result;
        private List<String> portList;

        DiscoveryFixture() {
            givenStubbing(mockConfig.getProperty("gps.discovery.keywords"))
                .willReturn(Optional.of("gps,u-blox,prolific,silicon labs,gnss,receiver"));
        }

        void givenMockHardwareHasPorts(final List<String> names) {
            final List<ISerialPort> ports = names.stream().map(name -> {
                final ISerialPort mockPort = mock(ISerialPort.class);
                givenStubbing(mockPort.getSystemPortName()).willReturn(name);
                return mockPort;
            }).toList();
            givenStubbing(mockProvider.getAvailablePorts()).willReturn(ports);
        }

        void givenMockHardwareHasPort(final String name, final String description) {
            final ISerialPort mockPort = mock(ISerialPort.class);
            givenStubbing(mockPort.getSystemPortName()).willReturn(name);
            givenStubbing(mockPort.getDescriptivePortName()).willReturn(name);
            givenStubbing(mockPort.getPortDescription()).willReturn(description);
            
            givenStubbing(mockProvider.getAvailablePorts()).willReturn(List.of(mockPort));
        }

        void whenScanning() {
            this.result = discovery.findLikelyGpsPort();
        }

        void whenListingPorts() {
            this.portList = discovery.getAvailablePorts();
        }

        void thenLikelyGpsIs(final String expected) {
            thenTrue(result.isPresent());
            then(result.get(), expected);
        }

        void thenNoLikelyGpsFound() {
            thenTrue(result.isEmpty());
        }

        void thenPortListContains(final String... expected) {
            java.util.Arrays.stream(expected).forEach(name -> thenTrue(portList.contains(name)));
        }
    }
}
