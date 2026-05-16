package com.stoicprogrammer.qtrqth.base;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Foundation for high-fidelity behavior-driven tests.
 * Provides access to the 'Test Data Vault'.
 */
public abstract class BddTest {

    /**
     * Loads a telemetry sample from the resources vault.
     * @param filename The name of the file in src/test/resources/telemetry/
     * @return A Stream of NMEA sentences.
     */
    protected Stream<String> loadTelemetrySample(final String filename) {
        try {
            final Path path = Paths.get(Objects.requireNonNull(
                getClass().getClassLoader().getResource("telemetry/" + filename)).toURI());
            return Files.lines(path);
        } catch (IOException | URISyntaxException | NullPointerException e) {
            throw new RuntimeException("Failed to load telemetry sample: " + filename, e);
        }
    }

    /**
     * Loads all sentences from a sample as a List.
     */
    protected List<String> getTelemetrySentences(final String filename) {
        return loadTelemetrySample(filename).toList();
    }
}
