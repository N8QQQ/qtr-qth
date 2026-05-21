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

    @Test
    void should_route_to_environment_doctor_when_flag_is_present() {
        // GIVEN: A capture for System.out
        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            // WHEN: Main is called with the --doctor flag
            Main.main(new String[]{"--doctor"});

            // THEN: The output should contain the doctor header
            assertThat(outContent.toString()).contains("🩺 qtr-qth: Environment Doctor");
        } finally {
            System.setOut(originalOut);
        }
    }
}
