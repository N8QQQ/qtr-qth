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

    private final SimulationFixture fixture = new SimulationFixture();

    @Test
    void should_provide_deterministic_ntp_response() {
        fixture.given_simulation_provider();
        fixture.when_getting_time("sim.ntp.org", TEST_TIMEOUT);
        fixture.then_response_is_present();
        fixture.then_rtt_is(EXPECTED_RTT);
        fixture.then_stratum_is(EXPECTED_STRATUM);
        fixture.then_root_dispersion_is(EXPECTED_DISPERSION);
        fixture.then_time_is_present();
    }

    private final class SimulationFixture {
        private SimulationNtpProvider provider;
        private Optional<NtpResponse> response = Optional.empty();

        void given_simulation_provider() {
            this.provider = new SimulationNtpProvider();
        }

        void when_getting_time(final String host, final int timeout) {
            this.response = provider.getTime(host, timeout);
        }

        void then_response_is_present() {
            assertThat(response).isPresent();
        }

        void then_rtt_is(final long expectedRtt) {
            assertThat(response).isPresent();
            assertThat(response.get().rttMilliseconds()).isEqualTo(expectedRtt);
        }

        void then_stratum_is(final int expectedStratum) {
            assertThat(response).isPresent();
            assertThat(response.get().stratum()).isEqualTo(expectedStratum);
        }

        void then_root_dispersion_is(final double expectedDispersion) {
            assertThat(response).isPresent();
            assertThat(response.get().rootDispersionMilliseconds()).isEqualTo(expectedDispersion);
        }

        void then_time_is_present() {
            assertThat(response).isPresent();
            assertThat(response.get().time()).isNotNull();
        }
    }
}
