package com.stoicprogrammer.qtrqth.ntp.simulation;

import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import com.stoicprogrammer.qtrqth.ntp.api.INtpProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

/**
 * Deterministic NTP simulation for hermetic testing and development.
 */
public final class SimulationNtpProvider implements INtpProvider {
    private static final Logger logger = LoggerFactory.getLogger(SimulationNtpProvider.class);

    // Hardcoded high-fidelity baseline for simulation
    private static final long SIM_RTT_MS = 25L;
    private static final int SIM_STRATUM = 2;
    private static final double SIM_DISPERSION = 10.0;

    @Override
    public Optional<NtpResponse> getTime(final String host, final int timeout) {
        logger.debug("Simulating NTP request to {}...", host);
        return Optional.of(new NtpResponse(
            Instant.now(), 
            SIM_RTT_MS, 
            SIM_STRATUM, 
            SIM_DISPERSION
        ));
    }
}
