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
    void givenNoConfigFile_whenInitializing_thenAllDefaultsAreLoaded() {
        fixture.givenNoConfigFile();
        fixture.whenInitializing();
        
        fixture.thenPropertyIs("ntp.server", "pool.ntp.org");
        fixture.thenPropertyIs("serial.baud", "9600");
        fixture.thenPropertyIs("sync.threshold.ms", "1000");
        fixture.thenPropertyIs("gps.discovery.keywords", "gps,u-blox,prolific,silicon labs,gnss,receiver");
        fixture.thenPropertyIs("display.raw.telemetry", "false");
        fixture.thenPropertyIs("simulation.mode", "true");
    }

    @Test
    void givenCustomConfigFile_whenInitializing_thenCustomValuesOverrideDefaults() throws IOException {
        fixture.givenCustomConfigFile("ntp.server=custom.ntp.org\nserial.baud=4800\nsimulation.mode=false");
        fixture.whenInitializing();
        
        fixture.thenPropertyIs("ntp.server", "custom.ntp.org");
        fixture.thenPropertyIs("serial.baud", "4800");
        fixture.thenPropertyIs("simulation.mode", "false");
        // Verify defaults still hold for unprovided keys
        fixture.thenPropertyIs("sync.threshold.ms", "1000");
    }

    @Test
    void givenReadOnlyPath_whenInitializing_thenHandlesSaveErrorGracefully() {
        // Use a path that is a directory instead of a file to trigger IOException on store()
        java.io.File dir = tempDir.resolve("not-a-file.properties").toFile();
        dir.mkdir();
        
        ConfigManager config = new ConfigManager(dir.getAbsolutePath());
        thenNotNull(config.getProperty("ntp.server"));
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
