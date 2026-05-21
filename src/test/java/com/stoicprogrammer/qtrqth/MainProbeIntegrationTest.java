package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the integrated hardware probe routing in the Main class.
 */
class MainProbeIntegrationTest extends BddTest {

    @Test
    void should_route_to_hardware_probe_when_flag_is_present() {
        // GIVEN: A capture for System.out
        final PrintStream originalOut = System.out;
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        try {
            // WHEN: Main is called with the --probe flag
            Main.main(new String[]{"--probe"});

            // THEN: The output should contain the hardware probe header
            assertThat(outContent.toString()).contains("📡 qtr-qth: Hardware Discovery Probe");
        } finally {
            System.setOut(originalOut);
        }
    }
}
