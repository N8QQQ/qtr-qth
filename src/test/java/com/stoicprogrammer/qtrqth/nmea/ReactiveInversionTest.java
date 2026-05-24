package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Red Phase: Certifying the Reactive Ingestion Pipeline.
 * Verifies Talker-Agnosticism and State Accumulation.
 */
class ReactiveInversionTest extends BddTest {

    private static final int MOCK_HOUR = 14;
    private static final int MOCK_MIN = 30;
    private static final int MOCK_SEC = 5;
    private static final int MOCK_SATS = 12;
    private static final double MOCK_SPEED = 0.5;

    @Test
    void should_accumulate_state_and_trigger_pulse_on_time_sentence() {
        // GIVEN: A talker-agnostic parser and a list of sentences from a u-blox M9N
        final NmeaParser parser = new NmeaParser();
        // Sample sentences without checksums for simple test setup
        final List<String> sentences = List.of(
            "$GNGGA,143005.00,4617.00579,N,08753.28148,W,1,12,0.85,425.1,M,-33.2,M,,",
            "$GNVTG,125.5,T,,M,0.5,N,0.9,K,A",
            "$GNZDA,143005.00,23,05,2026,00,00"
        );
        final List<GpsData> emittedStates = new CopyOnWriteArrayList<>();
        
        final GpsData initial = GpsData.EMPTY;

        // WHEN: Processing the stream reactively
        sentences.stream().reduce(
            initial, 
            (state, sentence) -> {
                final GpsData next = parser.parse(sentence, state);
                if (parser.isTrigger(sentence)) {
                    emittedStates.add(next);
                }
                return next;
            },
            (a, b) -> a // Combiner (unused in sequential stream)
        );

        // THEN: We should have 1 pulse (triggered by $GNZDA)
        assertThat(emittedStates).hasSize(1);

        // AND: The first pulse should have state merged from GNGGA and GNVTG
        final GpsData firstPulse = emittedStates.get(0);
        assertThat(firstPulse.utcTime()).isEqualTo(LocalTime.of(MOCK_HOUR, MOCK_MIN, MOCK_SEC));
        assertThat(firstPulse.satelliteCount()).isEqualTo(MOCK_SATS);
        assertThat(firstPulse.speedKnots()).isEqualTo(MOCK_SPEED); 
    }

    @Test
    void should_parse_gptxt_diagnostic_messages() {
        final NmeaParser parser = new NmeaParser();
        final String txtSentence = "$GPTXT,01,01,02,ANTSUPERV=AC SD PDoS SR*20";
        
        final GpsData state = parser.parse(txtSentence, GpsData.EMPTY);
        
        assertThat(state.latestDiagnostic()).isEqualTo("ANTSUPERV=AC SD PDoS SR");
    }

    @Test
    void should_be_talker_agnostic_supporting_gn_gl_and_ga() {
        final NmeaParser parser = new NmeaParser();
        
        // Asserting that modern talker IDs are supported
        assertThat(parser.isSupported("$GNRMC,120000,A,4617.0,N,08753.2,W,0.0,,230526,,,A")).isTrue();
        assertThat(parser.isSupported("$GLGGA,120000,4617.0,N,08753.2,W,1,08,1.0,100.0,M,,M,,")).isTrue();
        assertThat(parser.isSupported("$GAZDA,120000,23,05,2026,00,00")).isTrue();
    }
}
