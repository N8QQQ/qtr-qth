package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CalibrationEngineTest extends BddTest {

    @Test
    void should_identify_sentinel_after_confident_cycles() {
        final CalibrationEngine engine = new CalibrationEngine(3, 10);
        
        // Cycle 1: [RMC, GGA, ZDA]
        engine.observe("$GPRMC,123456,...", "123456.00");
        engine.observe("$GPGGA,123456,...", "123456.00");
        engine.observe("$GPZDA,123456,...", "123456.00");
        
        // Cycle 2: [RMC, GGA, ZDA] -> Rollover detected at first RMC of next second
        engine.observe("$GPRMC,123457,...", "123457.00"); // confidence: 1
        engine.observe("$GPGGA,123457,...", "123457.00");
        engine.observe("$GPZDA,123457,...", "123457.00");
        
        // Cycle 3: [RMC, GGA, ZDA]
        engine.observe("$GPRMC,123458,...", "123458.00"); // confidence: 2
        engine.observe("$GPGGA,123458,...", "123458.00");
        engine.observe("$GPZDA,123458,...", "123458.00");
        
        // Cycle 4: [RMC, GGA, ZDA]
        engine.observe("$GPRMC,123459,...", "123459.00"); // confidence: 2 -> 3 during NEXT rollover
        engine.observe("$GPGGA,123459,...", "123459.00");
        engine.observe("$GPZDA,123459,...", "123459.00");
        
        // Cycle 5: Trigger processing of Cycle 4
        final Optional<String> result = engine.observe("$GPRMC,123500,...", "123500.00");
        
        assertThat(result).contains("$GPZDA");
        assertThat(engine.isCalibrated()).isTrue();
        assertThat(engine.getSentinel()).isEqualTo("$GPZDA");
    }

    @Test
    void should_timeout_when_cadence_is_unstable() {
        final CalibrationEngine engine = new CalibrationEngine(3, 2);
        
        // Cycle 1: [RMC]
        engine.observe("$GPRMC,123456,...", "123456.00");
        
        // Cycle 2: [GGA] (Mismatch)
        engine.observe("$GPGGA,123457,...", "123457.00");
        
        // Cycle 3: Start -> Timeout
        engine.observe("$GPRMC,123458,...", "123458.00");
        
        assertThat(engine.isTimedOut()).isTrue();
        assertThat(engine.isCalibrated()).isFalse();
    }

    @Test
    void should_allow_forced_sentinel_to_bypass_calibration() {
        final CalibrationEngine engine = new CalibrationEngine(3, 10);
        engine.forceSentinel("$GPRMC");
        
        assertThat(engine.isCalibrated()).isTrue();
        assertThat(engine.getSentinel()).isEqualTo("$GPRMC");
    }
}
