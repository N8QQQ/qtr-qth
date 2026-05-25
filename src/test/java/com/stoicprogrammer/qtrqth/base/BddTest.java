package com.stoicprogrammer.qtrqth.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(BddTest.class);

    protected void reportGiven(final String step) {
        logger.info("[GIVEN] {}", step);
    }

    protected void reportWhen(final String step) {
        logger.info("[WHEN]  {}", step);
    }

    protected void reportThen(final String step) {
        logger.info("[THEN]  {}", step);
    }
    /**
     * Loads a file from the main resources tree.
     */
    protected List<String> loadMainResource(final String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path);
             var reader = new java.io.BufferedReader(new java.io.InputStreamReader(java.util.Objects.requireNonNull(is)))) {
            return reader.lines().toList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load main resource: " + path, e);
        }
    }

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
