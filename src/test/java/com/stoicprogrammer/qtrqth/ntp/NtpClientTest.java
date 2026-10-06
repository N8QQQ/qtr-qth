package com.stoicprogrammer.qtrqth.ntp;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.ntp.api.INtpProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class NtpClientTest extends BddTest {

    private static final int DEFAULT_TIMEOUT = 5000;
    private static final long MOCK_RTT = 10L;
    private static final long MOCK_RTT_SEC = 20L;
    private static final int STRATUM_ONE = 1;
    private static final int STRATUM_TWO = 2;
    private static final double DISPERSION_ONE = 1.0;
    private static final double DISPERSION_TWO = 2.0;

    private final NtpFixture fixture = new NtpFixture();

    @Test
    void should_poll_first_reachable_server_in_pool() {
        final NtpResponse expected = new NtpResponse(Instant.parse("2026-05-15T12:00:00Z"), MOCK_RTT, STRATUM_ONE, DISPERSION_ONE);
        fixture.given_server_returning("pool.ntp.org", expected);
        fixture.when_polling_pool(List.of("pool.ntp.org", "time.google.com"));
        fixture.then_result_contains(expected);
        fixture.then_provider_queried_once();
    }

    @Test
    void should_fallback_to_second_server_when_first_fails() {
        final NtpResponse expected = new NtpResponse(Instant.parse("2026-05-15T12:00:01Z"), MOCK_RTT_SEC, STRATUM_TWO, DISPERSION_TWO);
        fixture.given_server_failing("primary.ntp");
        fixture.given_server_returning("secondary.ntp", expected);
        fixture.when_polling_pool(List.of("primary.ntp", "secondary.ntp"));
        fixture.then_result_contains(expected);
    }

    @Test
    void should_return_empty_when_all_servers_fail() {
        fixture.given_all_servers_failing();
        fixture.when_polling_pool(List.of("bad1.ntp", "bad2.ntp"));
        fixture.then_result_is_empty();
    }

    @Test
    void should_return_empty_when_pool_is_empty() {
        fixture.when_polling_pool(List.of());
        fixture.then_result_is_empty();
    }

    private final class NtpFixture {
        private final INtpProvider mockProvider = mock(INtpProvider.class);
        private final NtpClient client = new NtpClient(mockProvider, DEFAULT_TIMEOUT);
        private Optional<NtpResponse> result = Optional.empty();

        void given_server_returning(final String server, final NtpResponse response) {
            given(mockProvider.getTime(server, DEFAULT_TIMEOUT)).willReturn(Optional.of(response));
        }

        void given_server_failing(final String server) {
            given(mockProvider.getTime(server, DEFAULT_TIMEOUT)).willReturn(Optional.empty());
        }

        void given_all_servers_failing() {
            given(mockProvider.getTime(anyString(), anyInt())).willReturn(Optional.empty());
        }

        void when_polling_pool(final List<String> pool) {
            this.result = client.pollDetailed(pool);
        }

        void then_result_contains(final NtpResponse expected) {
            assertThat(result).contains(expected);
        }

        void then_result_is_empty() {
            assertThat(result).isEmpty();
        }

        void then_provider_queried_once() {
            Mockito.verify(mockProvider, Mockito.times(1)).getTime(anyString(), anyInt());
        }
    }
}
