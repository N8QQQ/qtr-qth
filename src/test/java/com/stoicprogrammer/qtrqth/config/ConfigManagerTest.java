package com.stoicprogrammer.qtrqth.config;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigManagerTest extends BddTest {

    private static final int DEFAULT_BAUD = 9600;
    private static final long DEFAULT_SYNC_THRESHOLD = 1000L;

    @TempDir
    private Path tempDir;

    private final ManagerFixture fixture = new ManagerFixture();

    @Test
    void should_handle_load_failure_gracefully() {
        fixture.given_config_file_with_failing_loader("fail.properties");
        fixture.when_creating_manager();
        fixture.then_baud_is(DEFAULT_BAUD);
    }

    @Test
    void should_handle_save_failure_gracefully() {
        fixture.given_config_file_with_failing_saver("missing.properties");
        fixture.when_creating_manager();
        fixture.then_simulation_mode_is(false);
    }

    @Test
    void should_retrieve_raw_property_optional() throws IOException {
        fixture.given_file_with_content("raw.properties", "key=value");
        fixture.when_creating_manager();
        fixture.then_raw_property_contains("key", "value");
        fixture.then_raw_property_is_empty("missing");
    }

    @Test
    void should_handle_malformed_numeric_properties() throws IOException {
        fixture.given_file_with_content("malformed.properties", "serial.baud=INVALID\nsync.threshold.ms=BAD");
        fixture.when_creating_manager();
        fixture.then_baud_is(DEFAULT_BAUD);
        fixture.then_sync_threshold_is(DEFAULT_SYNC_THRESHOLD);
    }

    @Test
    void should_handle_missing_list_property() throws IOException {
        fixture.given_file_with_content("empty_list.properties", "other.key=value");
        fixture.when_creating_manager();
        fixture.then_ntp_pool_contains("pool.ntp.org");
    }

    private final class ManagerFixture {
        private Path configPath;
        private Optional<ConfigManager.FileAction> fileReader = Optional.empty();
        private Optional<ConfigManager.FileAction> fileWriter = Optional.empty();
        private ConfigManager manager;

        void given_config_file_with_failing_loader(final String filename) {
            this.configPath = tempDir.resolve(filename);
            this.fileReader = Optional.of((f, p) -> { throw new IOException("Disk Error"); });
            this.fileWriter = Optional.of((f, p) -> {});
        }

        void given_config_file_with_failing_saver(final String filename) {
            this.configPath = tempDir.resolve(filename);
            this.fileReader = Optional.of((f, p) -> {});
            this.fileWriter = Optional.of((f, p) -> { throw new IOException("Read Only"); });
        }

        void given_file_with_content(final String filename, final String content) throws IOException {
            this.configPath = tempDir.resolve(filename);
            Files.writeString(this.configPath, content);
            this.fileReader = Optional.empty();
            this.fileWriter = Optional.empty();
        }

        void when_creating_manager() {
            this.manager = fileReader.flatMap(r -> fileWriter.map(w -> new ConfigManager(configPath, r, w)))
                .orElseGet(() -> new ConfigManager(configPath));
        }

        void then_baud_is(final int expectedBaud) {
            assertThat(manager.getConfig().serialBaud()).isEqualTo(expectedBaud);
        }

        void then_sync_threshold_is(final long expectedThreshold) {
            assertThat(manager.getConfig().syncThresholdMilliseconds()).isEqualTo(expectedThreshold);
        }

        void then_simulation_mode_is(final boolean expectedSimMode) {
            assertThat(manager.getConfig().simulationMode()).isEqualTo(expectedSimMode);
        }

        void then_raw_property_contains(final String key, final String expectedValue) {
            assertThat(manager.getProperty(key)).contains(expectedValue);
        }

        void then_raw_property_is_empty(final String key) {
            assertThat(manager.getProperty(key)).isEmpty();
        }

        void then_ntp_pool_contains(final String expectedHost) {
            assertThat(manager.getConfig().ntpPool()).contains(expectedHost);
        }
    }
}
