package com.stoicprogrammer.qtrqth.clock.api;

import java.time.Duration;

/**
 * High-fidelity Hardware Abstraction Layer (HAL) for clock discipline.
 * Decouples the temporal adjustment logic from the OS-specific implementation.
 */
public interface IClockDiscipliner {
    /**
     * Applies a slew adjustment to the system clock.
     * @param offset The duration to adjust (positive for fast, negative for slow).
     */
    void slew(Duration offset);

    /**
     * Hard-steps the system clock to a specific instant.
     * Use with extreme caution—only for initial Stratum 0 sync.
     */
    void step(long epochMilli);

    /**
     * Returns true if this discipliner is allowed to modify physical hardware.
     */
    boolean isLive();
}
