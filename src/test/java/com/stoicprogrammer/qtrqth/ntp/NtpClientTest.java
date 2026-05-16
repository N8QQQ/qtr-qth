package com.stoicprogrammer.qtrqth.ntp;

import com.stoicprogrammer.qtrqth.ntp.NtpClient;
import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
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

class NtpClientTest {

    private static final int DEFAULT_TIMEOUT = 5000;
    private static final long MOCK_RTT = 10L;
    private static final long MOCK_RTT_SEC = 20L;
    private static final int STRATUM_ONE = 1;
    private static final int STRATUM_TWO = 2;
    private static final double DISPERSION_ONE = 1.0;
    private static final double DISPERSION_TWO = 2.0;

    private final INtpProvider mockProvider = mock(INtpProvider.class);
    private final NtpClient client = new NtpClient(mockProvider, DEFAULT_TIMEOUT);

    @Test
    void should_poll_first_reachable_server_in_pool() {
        final NtpResponse expected = new NtpResponse(Instant.parse("2026-05-15T12:00:00Z"), MOCK_RTT, STRATUM_ONE, DISPERSION_ONE);
        given(mockProvider.getTime("pool.ntp.org", DEFAULT_TIMEOUT)).willReturn(Optional.of(expected));
        
        final Optional<NtpResponse> result = client.pollDetailed(List.of("pool.ntp.org", "time.google.com"));
        
        assertThat(result).contains(expected);
        Mockito.verify(mockProvider, Mockito.times(1)).getTime(anyString(), anyInt());
    }

    @Test
    void should_fallback_to_second_server_when_first_fails() {
        final NtpResponse expected = new NtpResponse(Instant.parse("2026-05-15T12:00:01Z"), MOCK_RTT_SEC, STRATUM_TWO, DISPERSION_TWO);
        given(mockProvider.getTime("primary.ntp", DEFAULT_TIMEOUT)).willReturn(Optional.empty());
        given(mockProvider.getTime("secondary.ntp", DEFAULT_TIMEOUT)).willReturn(Optional.of(expected));
        
        final Optional<NtpResponse> result = client.pollDetailed(List.of("primary.ntp", "secondary.ntp"));
        
        assertThat(result).contains(expected);
    }

    @Test
    void should_return_empty_when_all_servers_fail() {
        given(mockProvider.getTime(anyString(), anyInt())).willReturn(Optional.empty());
        
        final Optional<NtpResponse> result = client.pollDetailed(List.of("bad1.ntp", "bad2.ntp"));
        
        assertThat(result).isEmpty();
    }

    @Test
    void should_return_empty_when_pool_is_empty() {
        final Optional<NtpResponse> result = client.pollDetailed(List.of());
        assertThat(result).isEmpty();
    }
}
