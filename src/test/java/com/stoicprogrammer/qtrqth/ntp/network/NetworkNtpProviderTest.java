package com.stoicprogrammer.qtrqth.ntp.network;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * High-fidelity verification of the Network NTP Provider.
 * Includes 'Live Network' guards for real-world connectivity tests.
 */
class NetworkNtpProviderTest extends BddTest {

    private final NetworkNtpProvider provider = new NetworkNtpProvider();

    @Test
    @EnabledIf("isNetworkLive")
    void should_poll_real_ntp_server_when_network_is_live() {
        final Optional<NtpResponse> response = provider.getTime("pool.ntp.org", 5000);
        
        assertThat(response).isPresent();
        assertThat(response.get().stratum()).isBetween(1, 15);
        assertThat(response.get().time()).isNotNull();
    }

    @Test
    void should_return_empty_on_invalid_hostname() {
        final Optional<NtpResponse> response = provider.getTime("invalid.hostname.n8qqq", 1000);
        assertThat(response).isEmpty();
    }

    /**
     * Guard Logic: Checks if the bridge can reach a reliable external IP.
     */
    static boolean isNetworkLive() {
        try {
            // Attempt to reach Google's public DNS (1.1.1.1 or 8.8.8.8)
            return InetAddress.getByName("8.8.8.8").isReachable(2000);
        } catch (IOException e) {
            return false;
        }
    }
}
