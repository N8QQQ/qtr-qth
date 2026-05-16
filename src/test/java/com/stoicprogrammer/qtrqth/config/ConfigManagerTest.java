package com.stoicprogrammer.qtrqth.config;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigManagerTest extends BddTest {

    private static final int DEFAULT_BAUD = 9600;

    @TempDir
    private Path tempDir;

    @Test
    void should_handle_load_failure_gracefully() {
        final Path configPath = tempDir.resolve("fail.properties");
        final ConfigManager manager = new ConfigManager(
            configPath,
            (f, p) -> { throw new IOException("Disk Error"); },
            (f, p) -> {}
        );
        
        assertThat(manager.getConfig().serialBaud()).isEqualTo(DEFAULT_BAUD); // Uses default
    }

    @Test
    void should_handle_save_failure_gracefully() {
        final Path configPath = tempDir.resolve("missing.properties");
        final ConfigManager manager = new ConfigManager(
            configPath,
            (f, p) -> {},
            (f, p) -> { throw new IOException("Read Only"); }
        );
        
        assertThat(manager.getConfig().simulationMode()).isTrue(); // Bootstrapped anyway
    }

    @Test
    void should_retrieve_raw_property_optional() throws IOException {
        final Path configPath = tempDir.resolve("raw.properties");
        java.nio.file.Files.writeString(configPath, "key=value");
        final ConfigManager manager = new ConfigManager(configPath);
        
        assertThat(manager.getProperty("key")).contains("value");
        assertThat(manager.getProperty("missing")).isEmpty();
    }

    @Test
    void should_handle_malformed_numeric_properties() throws IOException {
        final Path configPath = tempDir.resolve("malformed.properties");
        java.nio.file.Files.writeString(configPath, "serial.baud=INVALID\nsync.threshold.ms=BAD");
        final ConfigManager manager = new ConfigManager(configPath);
        
        assertThat(manager.getConfig().serialBaud()).isEqualTo(9600);
        assertThat(manager.getConfig().syncThresholdMs()).isEqualTo(1000L);
    }

    @Test
    void should_handle_missing_list_property() throws IOException {
        final Path configPath = tempDir.resolve("empty_list.properties");
        java.nio.file.Files.writeString(configPath, "other.key=value");
        final ConfigManager manager = new ConfigManager(configPath);
        
        // Should use hardcoded fallback in extractList
        assertThat(manager.getConfig().ntpPool()).contains("pool.ntp.org");
    }
}
