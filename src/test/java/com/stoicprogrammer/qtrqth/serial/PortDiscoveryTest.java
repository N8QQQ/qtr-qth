package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.config.AppConfig;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.serial.api.ISerialPort;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;

/**
 * Business Rule: [PHASE 2, STEP 2] - Serial Port Discovery.
 */
class PortDiscoveryTest extends BddTest {

    private final DiscoveryFixture fixture = new DiscoveryFixture();

    @Test
    void given_hardware_with_gps_when_scanning_then_gps_port_is_identified() {
        fixture.given_mock_hardware_has_port("COM5", "u-blox 7 - GPS/GNSS Receiver");
        fixture.when_scanning();
        fixture.then_likely_gps_is("COM5");
    }

    @Test
    void given_generic_hardware_when_scanning_then_no_likely_gps_found() {
        fixture.given_mock_hardware_has_port("COM1", "Generic Communications Port");
        fixture.when_scanning();
        fixture.then_no_likely_gps_found();
    }

    @Test
    void given_hardware_with_multiple_ports_when_listing_then_all_port_names_are_returned() {
        fixture.given_mock_hardware_has_ports(List.of("COM1", "COM2"));
        fixture.when_listing_ports();
        fixture.then_port_list_contains("COM1", "COM2");
    }

    private class DiscoveryFixture {
        private final ISerialProvider mockProvider = mock(ISerialProvider.class);
        private final ConfigManager mockConfigManager = mock(ConfigManager.class);
        private final PortDiscovery discovery;
        private Optional<String> result;
        private List<String> portList;

        DiscoveryFixture() {
            given(mockConfigManager.getConfig()).willReturn(AppConfig.DEFAULT);
            this.discovery = new PortDiscovery(mockProvider, mockConfigManager);
        }

        void given_mock_hardware_has_ports(final List<String> names) {
            final List<ISerialPort> ports = names.stream().map(name -> {
                final ISerialPort mockPort = mock(ISerialPort.class);
                given(mockPort.getSystemPortName()).willReturn(name);
                return mockPort;
            }).toList();
            given(mockProvider.getAvailablePorts()).willReturn(ports);
        }

        void given_mock_hardware_has_port(final String name, final String description) {
            final ISerialPort mockPort = mock(ISerialPort.class);
            given(mockPort.getSystemPortName()).willReturn(name);
            given(mockPort.getDescriptivePortName()).willReturn(name);
            given(mockPort.getPortDescription()).willReturn(description);
            
            given(mockProvider.getAvailablePorts()).willReturn(List.of(mockPort));
        }

        void when_scanning() {
            this.result = discovery.findLikelyGpsPort();
        }

        void when_listing_ports() {
            this.portList = discovery.getAvailablePorts();
        }

        void then_likely_gps_is(final String expected) {
            assertThat(result).isPresent().contains(expected);
        }

        void then_no_likely_gps_found() {
            assertThat(result).isEmpty();
        }

        void then_port_list_contains(final String... expected) {
            java.util.Arrays.stream(expected).forEach(name -> assertThat(portList).contains(name));
        }
    }
}
