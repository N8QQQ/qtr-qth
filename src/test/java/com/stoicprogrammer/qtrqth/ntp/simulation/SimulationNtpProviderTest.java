package com.stoicprogrammer.qtrqth.ntp.simulation;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SimulationNtpProvider.
 */
class SimulationNtpProviderTest extends BddTest {

    private static final int TEST_TIMEOUT = 1000;
    private static final long EXPECTED_RTT = 25L;
    private static final int EXPECTED_STRATUM = 2;
    private static final double EXPECTED_DISPERSION = 10.0;

    @Test
    void should_provide_deterministic_ntp_response() {
        final SimulationNtpProvider provider = new SimulationNtpProvider();
        final Optional<NtpResponse> response = provider.getTime("sim.ntp.org", TEST_TIMEOUT);

        assertThat(response).isPresent();
        assertThat(response.get().rttMilliseconds()).isEqualTo(EXPECTED_RTT);
        assertThat(response.get().stratum()).isEqualTo(EXPECTED_STRATUM);
        assertThat(response.get().rootDispersionMilliseconds()).isEqualTo(EXPECTED_DISPERSION);
        assertThat(response.get().time()).isNotNull();
    }
}
