package com.stoicprogrammer.qtrqth.ntp;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.ntp.api.INtpProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.BDDMockito.given;

/**
 * Business Rule: [PHASE 4.6] - CI Stabilization via NTP HAL.
 */
class NtpClientTest extends BddTest {

    private final NtpFixture fixture = new NtpFixture();

    @Test
    void given_a_valid_ntp_server_when_polling_detailed_then_return_rich_metadata() {
        fixture.given_mock_server_success("pool.ntp.org");
        fixture.when_polling_detailed();
        fixture.then_detailed_result_is_present_with_metadata();
    }

    @Test
    void given_a_valid_ntp_server_when_polling_then_return_valid_instant() {
        fixture.given_mock_server_success("pool.ntp.org");
        fixture.when_polling();
        fixture.then_result_is_present();
    }

    @Test
    void given_an_invalid_ntp_server_when_polling_then_return_empty_optional() {
        fixture.given_mock_server_failure("invalid.host");
        fixture.when_polling();
        fixture.then_result_is_empty();
    }

    @Test
    void given_a_list_of_servers_when_primary_fails_then_fallback_to_secondary() {
        fixture.given_mock_server_failure("primary.host");
        fixture.given_mock_server_success("secondary.host");
        fixture.given_servers(List.of("primary.host", "secondary.host"));
        fixture.when_polling_detailed();
        fixture.then_detailed_result_is_present_with_metadata();
    }

    @Test
    void given_all_servers_fail_when_polling_then_return_empty() {
        fixture.given_mock_server_failure("host.one");
        fixture.given_mock_server_failure("host.two");
        fixture.given_servers(List.of("host.one", "host.two"));
        fixture.when_polling_detailed();
        fixture.then_detailed_result_is_empty();
    }

    private class NtpFixture {
        private List<String> servers;
        private Optional<Instant> result;
        private Optional<NtpResponse> detailedResult;
        private final INtpProvider mockProvider = mock(INtpProvider.class);
        private final NtpClient client = new NtpClient(mockProvider, 3000);

        void given_mock_server_success(final String host) {
            this.servers = List.of(host);
            given(mockProvider.getTime(eq(host), anyInt()))
                .willReturn(Optional.of(new NtpResponse(Instant.now(), 10, 1, 5.0)));
        }

        void given_mock_server_failure(final String host) {
            this.servers = List.of(host);
            given(mockProvider.getTime(eq(host), anyInt())).willReturn(Optional.empty());
        }

        void given_servers(final List<String> servers) {
            this.servers = servers;
        }

        void when_polling() {
            this.result = client.poll(servers.get(0)); 
        }

        void when_polling_detailed() {
            this.detailedResult = client.pollDetailed(servers);
        }

        void then_result_is_present() {
            assertThat(result).isPresent();
        }

        void then_detailed_result_is_present_with_metadata() {
            assertThat(detailedResult).isPresent();
            final NtpResponse response = detailedResult.get();
            
            // Verify Metadata (RTT > 0, Stratum > 0, Dispersion >= 0)
            assertThat(response.rttMs()).isGreaterThanOrEqualTo(0);
            assertThat(response.stratum()).isGreaterThan(0);
            assertThat(response.rootDispersionMs()).isGreaterThanOrEqualTo(0);
        }

        void then_result_is_empty() {
            assertThat(result).isEmpty();
        }

        void then_detailed_result_is_empty() {
            assertThat(detailedResult).isEmpty();
        }
    }
}
