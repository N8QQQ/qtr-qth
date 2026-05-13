package com.stoicprogrammer.qtrqth.ntp;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Business Rule: [PHASE 4.2] - Baseline NTP Client.
 * Verify that the NTP client can reach a server and return a valid Instant.
 */
class NtpClientTest extends BddTest {

    private final NtpFixture fixture = new NtpFixture();

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
        private final NtpClient client = new NtpClient(3000);

        void givenServer(String server) {
            this.server = server;
        }

        void whenPolling() {
            this.result = client.poll(server);
        }

        void thenResultIsPresent() {
            thenTrue(result.isPresent());
            // Basic sanity: The time should be after the project's inception
            thenTrue(result.get().isAfter(Instant.parse("2025-01-01T00:00:00Z")));
        }

        void thenResultIsEmpty() {
            thenTrue(result.isEmpty());
        }
    }
}
