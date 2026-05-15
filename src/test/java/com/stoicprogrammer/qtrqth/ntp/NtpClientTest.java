package com.stoicprogrammer.qtrqth.ntp;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.ntp.api.INtpProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Business Rule: [PHASE 4.6] - CI Stabilization via NTP HAL.
 * Verify that the NTP client logic remains robust when using mocked network providers.
 */
class NtpClientTest extends BddTest {

    private final NtpFixture fixture = new NtpFixture();

    @Test
    void givenAValidNtpServer_whenPollingDetailed_thenReturnRichMetadata() {
        fixture.givenMockServerSuccess("pool.ntp.org");
        fixture.whenPollingDetailed();
        fixture.thenDetailedResultIsPresentWithMetadata();
    }

    @Test
    void givenAValidNtpServer_whenPolling_thenReturnValidInstant() {
        fixture.givenMockServerSuccess("pool.ntp.org");
        fixture.whenPolling();
        fixture.thenResultIsPresent();
    }

    @Test
    void givenAnInvalidNtpServer_whenPolling_thenReturnEmptyOptional() {
        fixture.givenMockServerFailure("invalid.host");
        fixture.whenPolling();
        fixture.thenResultIsEmpty();
    }

    @Test
    void givenAListOfServers_whenPrimaryFails_thenFallbackToSecondary() {
        fixture.givenMockServerFailure("primary.host");
        fixture.givenMockServerSuccess("secondary.host");
        fixture.givenServers(List.of("primary.host", "secondary.host"));
        fixture.whenPollingDetailed();
        fixture.thenDetailedResultIsPresentWithMetadata();
    }

    @Test
    void givenAllServersFail_whenPolling_thenReturnEmpty() {
        fixture.givenMockServerFailure("host.one");
        fixture.givenMockServerFailure("host.two");
        fixture.givenServers(List.of("host.one", "host.two"));
        fixture.whenPollingDetailed();
        fixture.thenDetailedResultIsEmpty();
    }

    private class NtpFixture {
        private List<String> servers;
        private Optional<Instant> result;
        private Optional<NtpResponse> detailedResult;
        private final INtpProvider mockProvider = mock(INtpProvider.class);
        private final NtpClient client = new NtpClient(mockProvider, 3000);

        void givenMockServerSuccess(final String host) {
            this.servers = List.of(host);
            when(mockProvider.getTime(eq(host), anyInt()))
                .thenReturn(Optional.of(new NtpResponse(Instant.now(), 10, 1, 5.0)));
        }

        void givenMockServerFailure(final String host) {
            this.servers = List.of(host);
            when(mockProvider.getTime(eq(host), anyInt())).thenReturn(Optional.empty());
        }

        void givenServers(final List<String> servers) {
            this.servers = servers;
        }

        void whenPolling() {
            this.result = client.poll(servers.get(0)); 
        }

        void whenPollingDetailed() {
            this.detailedResult = client.pollDetailed(servers);
        }

        void thenResultIsPresent() {
            thenTrue(result.isPresent());
        }

        void thenDetailedResultIsPresentWithMetadata() {
            thenTrue(detailedResult.isPresent());
            final NtpResponse response = detailedResult.get();
            
            // Verify Metadata (RTT > 0, Stratum > 0, Dispersion >= 0)
            thenTrue(response.rttMs() >= 0);
            thenTrue(response.stratum() > 0);
            thenTrue(response.rootDispersionMs() >= 0);
        }

        void thenResultIsEmpty() {
            thenTrue(result.isEmpty());
        }

        void thenDetailedResultIsEmpty() {
            thenTrue(detailedResult.isEmpty());
        }
    }
}
