package com.stoicprogrammer.qtrqth.config;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Business Rule: [PHASE 2, STEP 1] - Configuration Bootstrapping.
 */
class ConfigManagerTest extends BddTest {

    @TempDir
    Path tempDir;

    private final ConfigFixture fixture = new ConfigFixture();

    @Test
    void givenNoConfigFile_whenInitializing_thenDefaultsAreLoaded() {
        fixture.givenNoConfigFile();
        fixture.whenInitializing();
        fixture.thenPropertyIs("ntp.server", "pool.ntp.org");
        fixture.thenPropertyIs("serial.baud", "9600");
    }

    @Test
    void givenCustomConfigFile_whenInitializing_thenCustomValuesOverrideDefaults() throws IOException {
        fixture.givenCustomConfigFile("ntp.server=custom.ntp.org\nserial.baud=4800");
        fixture.whenInitializing();
        fixture.thenPropertyIs("ntp.server", "custom.ntp.org");
        fixture.thenPropertyIs("serial.baud", "4800");
    }

    private class ConfigFixture {
        private String configPath;
        private ConfigManager configManager;

        void givenNoConfigFile() {
            this.configPath = tempDir.resolve("non-existent.properties").toString();
        }

        void givenCustomConfigFile(String content) throws IOException {
            Path path = tempDir.resolve("custom.properties");
            java.nio.file.Files.writeString(path, content);
            this.configPath = path.toString();
        }

        void whenInitializing() {
            this.configManager = new ConfigManager(configPath);
        }

        void thenPropertyIs(String key, String expectedValue) {
            then(configManager.getProperty(key), expectedValue);
        }
    }
}
