package com.stoicprogrammer.qtrqth.ntp;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Business Rule: [PHASE 4.3] - Precision Metadata.
 * Verify that the NTP client can capture high-fidelity metadata (RTT, Stratum, Dispersion).
 */
class NtpClientTest extends BddTest {

    private final NtpFixture fixture = new NtpFixture();

    @Test
    void givenAValidNtpServer_whenPollingDetailed_thenReturnRichMetadata() {
        fixture.givenServer("pool.ntp.org");
        fixture.whenPollingDetailed();
        fixture.thenDetailedResultIsPresentWithMetadata();
    }

    @Test
    void givenAValidNtpServer_whenPolling_thenReturnValidInstant() {
        fixture.givenServer("pool.ntp.org");
        fixture.whenPolling();
        fixture.thenResultIsPresent();
    }

    @Test
    void givenAnInvalidNtpServer_whenPolling_thenReturnEmptyOptional() {
        fixture.givenServer("invalid.server.address.internal");
        fixture.whenPolling();
        fixture.thenResultIsEmpty();
    }

    private class NtpFixture {
        private String server;
        private Optional<Instant> result;
        private Optional<NtpResponse> detailedResult;
        private final NtpClient client = new NtpClient(3000);

        void givenServer(String server) {
            this.server = server;
        }

        void whenPolling() {
            this.result = client.poll(server);
        }

        void whenPollingDetailed() {
            this.detailedResult = client.pollDetailed(server);
        }

        void thenResultIsPresent() {
            thenTrue(result.isPresent());
            thenTrue(result.get().isAfter(Instant.parse("2025-01-01T00:00:00Z")));
        }

        void thenDetailedResultIsPresentWithMetadata() {
            thenTrue(detailedResult.isPresent());
            NtpResponse response = detailedResult.get();
            
            // Verify time
            thenTrue(response.time().isAfter(Instant.parse("2025-01-01T00:00:00Z")));
            
            // Verify Metadata (RTT > 0, Stratum > 0, Dispersion >= 0)
            thenTrue(response.rttMs() >= 0);
            thenTrue(response.stratum() > 0);
            thenTrue(response.rootDispersionMs() >= 0);
        }

        void thenResultIsEmpty() {
            thenTrue(result.isEmpty());
        }
    }
}
