package com.stoicprogrammer.qtrqth.config;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Business Rule: [PHASE 6.1] - Typed Configuration Pipeline.
 */
class AppConfigTest extends BddTest {

    @TempDir
    private Path tempDir;

    private final ConfigFixture fixture = new ConfigFixture();

    @Test
    void should_load_typed_config_with_overrides_when_custom_file_provided() throws IOException {
        fixture.given_custom_config_file("ntp.server=custom.ntp.org\nserial.baud=4800\nsimulation.mode=false");
        fixture.when_loading_typed_config();
        fixture.then_config_baud_is(4800);
        fixture.then_config_sim_mode_is(false);
        fixture.then_config_ntp_pool_contains("custom.ntp.org");
    }

    @Test
    void should_fallback_to_defaults_when_config_is_malformed() throws IOException {
        fixture.given_custom_config_file("serial.baud=INVALID_NUMBER");
        fixture.when_loading_typed_config();
        fixture.then_config_baud_is(9600); // Default
    }

    private class ConfigFixture {
        private Path configPath;
        private AppConfig config;

        void given_custom_config_file(final String content) throws IOException {
            final Path path = tempDir.resolve("typed.properties");
            java.nio.file.Files.writeString(path, content);
            this.configPath = path;
        }

        void when_loading_typed_config() {
            final ConfigManager manager = new ConfigManager(configPath);
            this.config = manager.getConfig();
        }

        void then_config_baud_is(final int expected) {
            assertThat(config.serialBaud()).isEqualTo(expected);
        }

        void then_config_sim_mode_is(final boolean expected) {
            assertThat(config.simulationMode()).isEqualTo(expected);
        }

        void then_config_ntp_pool_contains(final String expected) {
            assertThat(config.ntpPool()).contains(expected);
        }
    }
}
