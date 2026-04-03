package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        private String result;
        private List<String> portList;

        DiscoveryFixture() {
            givenStubbing(mockConfig.getProperty("gps.discovery.keywords"))
                .willReturn("gps,u-blox,prolific,silicon labs,gnss,receiver");
        }

        void givenMockHardwareHasPorts(List<String> names) {
            List<ISerialPort> ports = new java.util.ArrayList<>();
            for (String name : names) {
                ISerialPort mockPort = mock(ISerialPort.class);
                givenStubbing(mockPort.getSystemPortName()).willReturn(name);
                ports.add(mockPort);
            }
            givenStubbing(mockProvider.getAvailablePorts()).willReturn(ports);
        }

        void givenMockHardwareHasPort(String name, String description) {
            ISerialPort mockPort = mock(ISerialPort.class);
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

        void thenLikelyGpsIs(String expected) {
            then(result, expected);
        }

        void thenNoLikelyGpsFound() {
            then(result, (String) null);
        }

        void thenPortListContains(String... expected) {
            for (String name : expected) {
                thenTrue(portList.contains(name));
            }
        }
    }
}
