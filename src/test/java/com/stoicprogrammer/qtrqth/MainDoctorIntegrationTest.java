package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the integrated environment doctor routing in the Main class.
 */
class MainDoctorIntegrationTest extends BddTest {

    private final CliFixture fixture = new CliFixture();

    @Test
    void should_route_to_environment_doctor_when_flag_is_present() {
        fixture.when_executing_main("--doctor");
        fixture.then_output_contains("🩺 qtr-qth: Environment Doctor");
    }

    private final class CliFixture {
        private String output = "";

        void when_executing_main(final String... args) {
            final PrintStream originalOut = System.out;
            final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
            try {
                Main.main(args);
                this.output = outContent.toString();
            } finally {
                System.setOut(originalOut);
            }
        }

        void then_output_contains(final String expectedSubstring) {
            assertThat(output).contains(expectedSubstring);
        }
    }
}
