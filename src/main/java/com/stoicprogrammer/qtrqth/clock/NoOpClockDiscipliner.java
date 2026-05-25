package com.stoicprogrammer.qtrqth.clock;

import com.stoicprogrammer.qtrqth.clock.api.IClockDiscipliner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * Safety-locked discipliner for testing and non-root environments.
 * Logs intended adjustments but performs no physical hardware modification.
 */
public final class NoOpClockDiscipliner implements IClockDiscipliner {
    private static final Logger logger = LoggerFactory.getLogger(NoOpClockDiscipliner.class);

    @Override
    public void slew(final Duration offset) {
        logger.info("[CLOCK-SAFETY] Intended Slew Adjustment: {} ms (IGNORED)", offset.toMillis());
    }

    @Override
    public void step(final long epochMilli) {
        logger.warn("[CLOCK-SAFETY] Intended Hard Step: {} ms (IGNORED)", epochMilli);
    }

    @Override
    public boolean isLive() {
        return false;
    }
}
