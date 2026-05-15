package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TelemetryPulseTest extends BddTest {

    private final PulseFixture fixture = new ConnectorFixture();

    @Test
    void givenAValidSentence_whenStartingPulse_thenIdIsGenerated() {
        fixture.givenSentence("$GPRMC,123456,A*66");
        fixture.whenStartingPulse();
        fixture.thenIdIsNotEmpty();
    }

    @Test
    void givenAPulse_whenUpdating_thenParserIsCalledAndStateUpdated() {
        fixture.givenSentence("$GPRMC,123456,A*66");
        fixture.givenStartingPulse();
        fixture.givenMockParserReturns(new GpsData(LocalTime.now(), null, 0, 0, 0, 0));
        
        fixture.whenUpdatingPulse();
        
        fixture.thenFixIsValid();
    }

    private abstract static class PulseFixture {
        abstract void givenSentence(String s);
        abstract void givenStartingPulse();
        abstract void givenMockParserReturns(GpsData data);
        abstract void whenStartingPulse();
        abstract void whenUpdatingPulse();
        abstract void thenIdIsNotEmpty();
        abstract void thenFixIsValid();
    }

    private class ConnectorFixture extends PulseFixture {
        private String sentence;
        private Main.TelemetryPulse pulse;
        private final NmeaParser mockParser = mock(NmeaParser.class);
        private final AtomicReference<GpsData> state = new AtomicReference<>(new GpsData(null, null, 0, 0, 0, 0));
        private final NtpResponse mockNtp = new NtpResponse(Instant.now(), 10, 1, 5.0);

        @Override
        void givenSentence(final String s) {
            this.sentence = s;
        }

        @Override
        void givenStartingPulse() {
            this.pulse = Main.TelemetryPulse.start(sentence, mockNtp);
        }

        @Override
        void givenMockParserReturns(final GpsData data) {
            when(mockParser.parse(eq(sentence), any())).thenReturn(data);
        }

        @Override
        void whenStartingPulse() {
            this.pulse = Main.TelemetryPulse.start(sentence, mockNtp);
        }

        @Override
        void whenUpdatingPulse() {
            this.pulse = pulse.update(mockParser, state);
        }

        @Override
        void thenIdIsNotEmpty() {
            thenTrue(pulse.pulseId() != null && !pulse.pulseId().isEmpty());
        }

        @Override
        void thenFixIsValid() {
            thenTrue(pulse.hasValidFix());
        }
    }
}
