package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Business Rule: [PHASE 3, STEP 6] - Telemetry Tracing.
 * Verify that the TelemetryPulse wrapper correctly manages Trace IDs and state.
 */
class TelemetryPulseTest extends BddTest {

    private final PulseFixture fixture = new AccumulatorFixture();

    @Test
    void givenARawSentence_whenStartingPulse_thenUniqueShortIdIsGenerated() {
        fixture.givenSentence("$GPRMC,123456,A,40,N,080,W,0,0,010126,,,A*66");
        fixture.whenStartingPulse();
        fixture.thenPulseIdIsGenerated();
    }

    @Test
    void givenAnActivePulse_whenUpdatingState_thenParserIsCalledAndResultReturned() {
        fixture.givenSentence("$GPRMC,123456,A,40,N,080,W,0,0,010126,,,A*66");
        fixture.givenMockParserWithResult(new GpsData(java.time.LocalTime.of(12,0,0), null, 40, -80, 0, 0));
        fixture.whenStartingPulse();
        fixture.whenUpdatingPulse();
        fixture.thenDataIsUpdated();
    }

    @Test
    void givenAPulseWithData_whenLogging_thenMdcContextIsManaged() {
        fixture.givenSentence("$GPRMC,123456,A,40,N,080,W,0,0,010126,,,A*66");
        fixture.givenMockParserWithResult(new GpsData(java.time.LocalTime.of(12,0,0), null, 40, -80, 0, 0));
        fixture.whenStartingPulse();
        fixture.whenUpdatingPulse();
        fixture.whenLoggingFinal();
        fixture.thenMdcWasCleared();
    }

    private class PulseFixture {
        private String sentence;
        private Main.TelemetryPulse pulse;
        private final NmeaParser mockParser = mock(NmeaParser.class);
        private final Logger mockLogger = mock(Logger.class);
        private final AtomicReference<GpsData> state = new AtomicReference<>(new GpsData(null, null, 0, 0, 0, 0));
        private GpsData expectedResult;

        void givenSentence(String s) {
            this.sentence = s;
        }

        void givenMockParserWithResult(GpsData data) {
            this.expectedResult = data;
            when(mockParser.parse(eq(sentence), any())).thenReturn(data);
        }

        void whenStartingPulse() {
            this.pulse = Main.TelemetryPulse.start(sentence);
        }

        void whenUpdatingPulse() {
            this.pulse = pulse.update(mockParser, state);
        }

        void whenLoggingFinal() {
            pulse.logFinal(mockLogger);
        }

        void thenPulseIdIsGenerated() {
            thenNotNull(pulse.pulseId());
            thenTrue(pulse.pulseId().length() == 4);
        }

        void thenDataIsUpdated() {
            then(pulse.data(), expectedResult);
            then(state.get(), expectedResult);
        }

        void thenMdcWasCleared() {
            // If we are here, MDC.clear() was called in the finally block
            then(MDC.get("pulseId"), null);
        }
    }

    // Alignment with BDD standard
    private class AccumulatorFixture extends PulseFixture {}
}
