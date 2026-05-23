package com.stoicprogrammer.qtrqth.serial;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CalibrationEngineTest extends BddTest {

    @Test
    void should_identify_sentinel_based_on_burst_end_consistency() {
        final CalibrationEngine engine = new CalibrationEngine(3, 10);
        
        // Cycle 1: [RMC, GGA, ZDA]
        engine.observe("$GPRMC,123456,...", "123456");
        engine.observe("$GPGGA,123456,...", "123456");
        engine.observe("$GPZDA,123456,...", "123456");
        
        // Cycle 2: [RMC, ZDA] (Missing GGA - should still lock on ZDA later)
        engine.observe("$GPRMC,123457,...", "123457");
        engine.observe("$GPZDA,123457,...", "123457"); // confidence: 1 (ZDA matched ZDA)
        
        // Cycle 3: [RMC, GGA, GSV, ZDA] (Extra noise - should still lock on ZDA)
        engine.observe("$GPRMC,123458,...", "123458");
        engine.observe("$GPGGA,123458,...", "123458");
        engine.observe("$GPGSV,123458,...", ""); // Metadata ignored by rollover logic but adds to burst
        engine.observe("$GPZDA,123458,...", "123458"); // confidence: 2
        
        // Cycle 4: [RMC, GGA, ZDA]
        engine.observe("$GPRMC,123459,...", "123459"); // confidence: 2 -> 3 during NEXT rollover
        engine.observe("$GPGGA,123459,...", "123459");
        engine.observe("$GPZDA,123459,...", "123459");
        
        // Cycle 5: Trigger processing of Cycle 4
        final Optional<String> result = engine.observe("$GPRMC,123500,...", "123500");
        
        assertThat(result).contains("$GPZDA");
        assertThat(engine.isCalibrated()).isTrue();
        assertThat(engine.getSentinel()).isEqualTo("$GPZDA");
    }

    @Test
    void should_timeout_when_sentinel_is_inconsistent() {
        final CalibrationEngine engine = new CalibrationEngine(3, 2);
        
        // Cycle 1 ends with RMC
        engine.observe("$GPRMC,123456,...", "123456");
        
        // Cycle 2 ends with GGA (Mismatch)
        engine.observe("$GPRMC,123457,...", "123457");
        engine.observe("$GPGGA,123457,...", "123457");
        
        // Cycle 3 start -> Timeout
        engine.observe("$GPRMC,123458,...", "123458");
        
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
