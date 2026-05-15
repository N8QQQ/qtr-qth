package com.stoicprogrammer.qtrqth.config;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Business Rule: [PHASE 2, STEP 1] - Configuration Bootstrapping.
 */
class ConfigManagerTest extends BddTest {

    @TempDir
    private Path tempDir;

    private final ConfigFixture fixture = new ConfigFixture();

    @Test
    void given_no_config_file_when_initializing_then_all_defaults_are_loaded() {
        fixture.given_no_config_file();
        fixture.when_initializing();
        
        fixture.then_property_is("ntp.server", "pool.ntp.org,time.google.com,time.windows.com");
        fixture.then_property_is("serial.baud", "9600");
        fixture.then_property_is("sync.threshold.ms", "1000");
        fixture.then_property_is("gps.discovery.keywords", "gps,u-blox,prolific,silicon labs,gnss,receiver");
        fixture.then_property_is("display.raw.telemetry", "false");
        fixture.then_property_is("simulation.mode", "true");
    }

    @Test
    void given_custom_config_file_when_initializing_then_custom_values_override_defaults() throws IOException {
        fixture.given_custom_config_file("ntp.server=custom.ntp.org\nserial.baud=4800\nsimulation.mode=false");
        fixture.when_initializing();
        
        fixture.then_property_is("ntp.server", "custom.ntp.org");
        fixture.then_property_is("serial.baud", "4800");
        fixture.then_property_is("simulation.mode", "false");
        fixture.then_property_is("sync.threshold.ms", "1000");
    }

    @Test
    void given_read_only_path_when_initializing_then_handles_save_error_gracefully() {
        final java.io.File dir = tempDir.resolve("not-a-file.properties").toFile();
        dir.mkdir();
        
        final ConfigManager config = new ConfigManager(dir.getAbsolutePath());
        assertThat(config.getProperty("ntp.server")).isPresent();
    }

    private class ConfigFixture {
        private String configPath;
        private ConfigManager configManager;

        void given_no_config_file() {
            this.configPath = tempDir.resolve("non-existent.properties").toString();
        }

        void given_custom_config_file(final String content) throws IOException {
            final Path path = tempDir.resolve("custom.properties");
            java.nio.file.Files.writeString(path, content);
            this.configPath = path.toString();
        }

        void when_initializing() {
            this.configManager = new ConfigManager(configPath);
        }

        void then_property_is(final String key, final String expectedValue) {
            final Optional<String> prop = configManager.getProperty(key);
            assertThat(prop).isPresent().contains(expectedValue);
        }
    }
}
