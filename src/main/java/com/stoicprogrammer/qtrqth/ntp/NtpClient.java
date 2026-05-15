package com.stoicprogrammer.qtrqth.ntp;

import com.stoicprogrammer.qtrqth.ntp.api.INtpProvider;
import com.stoicprogrammer.qtrqth.ntp.network.NetworkNtpProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Robust NTP Client for capturing network-based time references using declarative patterns.
 * Adheres to strict finality and functional purity standards.
 */
public final class NtpClient {
    private static final Logger logger = LoggerFactory.getLogger(NtpClient.class);
    private final INtpProvider provider;
    private final int timeoutMs;

    public NtpClient(final int timeoutMs) {
        this(new NetworkNtpProvider(), timeoutMs);
    }

    public NtpClient(final INtpProvider provider, final int timeoutMs) {
        this.provider = provider;
        this.timeoutMs = timeoutMs;
    }

    /**
     * Polls the specified NTP servers for high-fidelity time and metadata.
     * Tries each server in the list until one succeeds.
     * @param hostnames The list of NTP server addresses.
     * @return An Optional containing the NtpResponse, or empty if all polls failed.
     */
    public Optional<NtpResponse> pollDetailed(final List<String> hostnames) {
        return Optional.ofNullable(hostnames)
            .stream()
            .flatMap(List::stream)
            .map(host -> provider.getTime(host, timeoutMs))
            .flatMap(Optional::stream)
            .findFirst()
            .or(() -> {
                final boolean hasServers = Optional.ofNullable(hostnames).filter(l -> !l.isEmpty()).isPresent();
                Map.<Boolean, Runnable>of(
                    true, () -> logger.warn("All NTP polls failed for pool: {}", hostnames),
                    false, () -> {}
                ).get(hasServers).run();
                return Optional.empty();
            });
    }

    /**
     * Polls the specified NTP server for high-fidelity time and metadata.
     * @param hostname The NTP server address.
     * @return An Optional containing the NtpResponse, or empty if the poll failed.
     */
    public Optional<NtpResponse> pollDetailed(final String hostname) {
        return pollDetailed(List.of(hostname));
    }

    /**
     * Polls the specified NTP server for the current time.
     * @param hostname The NTP server address (e.g., pool.ntp.org).
     * @return An Optional containing the Instant, or empty if the poll failed.
     */
    public Optional<Instant> poll(final String hostname) {
        return pollDetailed(hostname).map(NtpResponse::time);
    }
}
