package com.stoicprogrammer.qtrqth.ntp.simulation;

import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import com.stoicprogrammer.qtrqth.ntp.api.INtpProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.InstantSource;
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

    private final InstantSource clock;

    public SimulationNtpProvider() {
        this(InstantSource.system());
    }

    public SimulationNtpProvider(final InstantSource clock) {
        this.clock = clock;
    }

    @Override
    public Optional<NtpResponse> getTime(final String host, final int timeout) {
        logger.debug("Simulating NTP request to {}...", host);
        return Optional.of(new NtpResponse(
            clock.instant(), 
            SIM_RTT_MS, 
            SIM_STRATUM, 
            SIM_DISPERSION
        ));
    }
}
