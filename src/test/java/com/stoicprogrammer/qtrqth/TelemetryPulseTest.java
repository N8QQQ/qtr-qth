package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.nmea.NmeaParser;
import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class TelemetryPulseTest extends BddTest {

    private final PulseFixture fixture = new ConnectorFixture();

    @Test
    void given_a_valid_sentence_when_starting_pulse_then_id_is_generated() {
        fixture.given_sentence("$GPRMC,123456,A*66");
        fixture.when_starting_pulse();
        fixture.then_id_is_not_empty();
    }

    @Test
    void given_a_pulse_when_updating_then_parser_is_called_and_state_updated() {
        fixture.given_sentence("$GPRMC,123456,A*66");
        fixture.given_starting_pulse();
        fixture.given_mock_parser_returns(new GpsData(LocalTime.now(), null, 0, 0, 0, 0));
        
        fixture.when_updating_pulse();
        
        fixture.then_fix_is_valid();
    }

    private abstract static class PulseFixture {
        abstract void given_sentence(String s);
        abstract void given_starting_pulse();
        abstract void given_mock_parser_returns(GpsData data);
        abstract void when_starting_pulse();
        abstract void when_updating_pulse();
        abstract void then_id_is_not_empty();
        abstract void then_fix_is_valid();
    }

    private static class ConnectorFixture extends PulseFixture {
        private String sentence;
        private TelemetryPulse pulse;
        private final NmeaParser mockParser = mock(NmeaParser.class);
        private final AtomicReference<GpsData> state = new AtomicReference<>(new GpsData(null, null, 0, 0, 0, 0));
        private final NtpResponse mockNtp = new NtpResponse(Instant.now(), 10, 1, 5.0);

        @Override
        void given_sentence(final String s) {
            this.sentence = s;
        }

        @Override
        void given_starting_pulse() {
            this.pulse = TelemetryPulse.start(sentence, mockNtp);
        }

        @Override
        void given_mock_parser_returns(final GpsData data) {
            given(mockParser.parse(eq(sentence), any())).willReturn(data);
        }

        @Override
        void when_starting_pulse() {
            this.pulse = TelemetryPulse.start(sentence, mockNtp);
        }

        @Override
        void when_updating_pulse() {
            this.pulse = pulse.update(mockParser, state);
        }

        @Override
        void then_id_is_not_empty() {
            assertThat(pulse.pulseId()).isNotNull().isNotEmpty();
        }

        @Override
        void then_fix_is_valid() {
            assertThat(pulse.hasValidFix()).isTrue();
        }
    }
}
