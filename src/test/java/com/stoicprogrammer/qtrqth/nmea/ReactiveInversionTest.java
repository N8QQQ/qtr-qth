package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    private static final int EXPECTED_PULSE_COUNT = 1;
    private static final long TIMEOUT_SECONDS = 3L;

    private final ReactiveFixture fixture = new ReactiveFixture();

    @Test
    void should_accumulate_state_and_trigger_pulse_on_time_sentence() throws InterruptedException {
        fixture.given_sentences(List.of(
            "$GNGGA,143005.00,4617.00579,N,08753.28148,W,1,12,0.85,425.1,M,-33.2,M,,",
            "$GNVTG,125.5,T,,M,0.5,N,0.9,K,A",
            "$GNZDA,143005.00,23,05,2026,00,00"
        ));
        fixture.when_processing_stream();
        fixture.then_latch_completes_within(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        fixture.then_emitted_states_count_is(EXPECTED_PULSE_COUNT);
        fixture.then_first_pulse_time_is(MOCK_HOUR, MOCK_MIN, MOCK_SEC);
        fixture.then_first_pulse_satellite_count_is(MOCK_SATS);
        fixture.then_first_pulse_speed_knots_is(MOCK_SPEED);
    }

    @Test
    void should_parse_gptxt_diagnostic_messages() {
        fixture.given_sentence("$GPTXT,01,01,02,ANTSUPERV=AC SD PDoS SR*20");
        fixture.when_parsing_single_sentence();
        fixture.then_latest_diagnostic_is("ANTSUPERV=AC SD PDoS SR");
    }

    @Test
    void should_be_talker_agnostic_supporting_gn_gl_and_ga() {
        fixture.then_sentence_is_supported("$GNRMC,120000,A,4617.0,N,08753.2,W,0.0,,230526,,,A");
        fixture.then_sentence_is_supported("$GLGGA,120000,4617.0,N,08753.2,W,1,08,1.0,100.0,M,,M,,");
        fixture.then_sentence_is_supported("$GAZDA,120000,23,05,2026,00,00");
    }

    private final class ReactiveFixture {
        private final NmeaParser parser = new NmeaParser();
        private List<String> inputSentences = List.of();
        private String singleSentence;
        private final List<GpsData> emittedStates = new CopyOnWriteArrayList<>();
        private GpsData lastState = GpsData.EMPTY;
        private final CountDownLatch latch = new CountDownLatch(EXPECTED_PULSE_COUNT);

        void given_sentences(final List<String> sentences) {
            this.inputSentences = sentences;
        }

        void given_sentence(final String sentence) {
            this.singleSentence = sentence;
        }

        void when_processing_stream() {
            this.lastState = inputSentences.stream().reduce(
                GpsData.EMPTY,
                (state, sentence) -> {
                    final GpsData next = parser.parse(sentence, state);
                    if (parser.isTrigger(sentence)) {
                        emittedStates.add(next);
                        latch.countDown();
                    }
                    return next;
                },
                (a, b) -> a
            );
        }

        void when_parsing_single_sentence() {
            this.lastState = parser.parse(singleSentence, GpsData.EMPTY);
        }

        void then_latch_completes_within(final long timeout, final TimeUnit unit) throws InterruptedException {
            final boolean completed = latch.await(timeout, unit);
            assertThat(completed)
                .as("Timed out waiting for reactive stream emission within %d %s", timeout, unit)
                .isTrue();
        }

        void then_emitted_states_count_is(final int expected) {
            assertThat(emittedStates).hasSize(expected);
        }

        void then_first_pulse_time_is(final int hour, final int minute, final int second) {
            assertThat(emittedStates).isNotEmpty();
            assertThat(emittedStates.get(0).utcTime()).isEqualTo(LocalTime.of(hour, minute, second));
        }

        void then_first_pulse_satellite_count_is(final int expected) {
            assertThat(emittedStates).isNotEmpty();
            assertThat(emittedStates.get(0).satelliteCount()).isEqualTo(expected);
        }

        void then_first_pulse_speed_knots_is(final double expected) {
            assertThat(emittedStates).isNotEmpty();
            assertThat(emittedStates.get(0).speedKnots()).isEqualTo(expected);
        }

        void then_latest_diagnostic_is(final String expected) {
            assertThat(lastState.latestDiagnostic()).isEqualTo(expected);
        }

        void then_sentence_is_supported(final String sentence) {
            assertThat(parser.isSupported(sentence)).isTrue();
        }
    }
}
