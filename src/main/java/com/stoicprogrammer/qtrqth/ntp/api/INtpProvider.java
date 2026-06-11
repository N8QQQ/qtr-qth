package com.stoicprogrammer.qtrqth.ntp.api;

import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import java.util.Optional;

/**
 * Hardware Abstraction Layer (HAL) for NTP network operations.
 * Allows decoupling high-level polling logic from the underlying network implementation.
 */
@FunctionalInterface
public interface INtpProvider {
    /**
     * Attempts to fetch high-fidelity time from a specific NTP host.
     * @param hostname The NTP server address.
     * @param timeoutMs The network timeout in milliseconds.
     * @return An Optional containing the NtpResponse, or empty if the operation failed.
     */
    Optional<NtpResponse> getTime(String hostname, int timeoutMilliseconds);
}
