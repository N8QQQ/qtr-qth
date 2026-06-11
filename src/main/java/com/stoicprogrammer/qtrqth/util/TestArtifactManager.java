package com.stoicprogrammer.qtrqth.util;

import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Secure artifact manager for high-fidelity testing.
 * Provides Tamper-Evident checksum validation for raw telemetry datasets.
 */
public final class TestArtifactManager {
    private static final Logger logger = LoggerFactory.getLogger(TestArtifactManager.class);
    private static final String ALGORITHM = "SHA-256";

    private TestArtifactManager() {
        // Utility Class
    }

    /**
     * Secures a generated NMEA dataset by writing it to disk along with its SHA-256 hash.
     * @param targetPath The primary NMEA data file path.
     * @param sentences The raw NMEA sentences to save.
     * @return The calculated SHA-256 hash.
     */
    public static String secureDataset(final Path targetPath, final List<String> sentences) {
        return Try.of(() -> {
            Files.write(targetPath, sentences);
            final String hash = calculateHash(targetPath);
            final Path hashPath = targetPath.resolveSibling(targetPath.getFileName() + ".sha256");
            Files.writeString(hashPath, hash);
            logger.info("Dataset Secured: {} | Hash: {}", targetPath.getFileName(), hash);
            return hash;
        }).getOrElseThrow(e -> new RuntimeException("Failed to secure test artifact", e));
    }

    /**
     * Verifies the cryptographic integrity of a test dataset.
     * @param targetPath The NMEA data file path.
     * @return true if the current file matches the expected hash.
     */
    public static boolean verifyDataset(final Path targetPath) {
        final Path hashPath = targetPath.resolveSibling(targetPath.getFileName() + ".sha256");
        
        return Optional.of(hashPath)
            .filter(Files::exists)
            .flatMap(p -> Try.of(() -> Files.readString(p).trim()).toJavaOptional())
            .map(expectedHash -> {
                final String actualHash = calculateHash(targetPath);
                final boolean valid = actualHash.equals(expectedHash);
                
                // Pure Functional Logging
                Map.<Boolean, Runnable>of(
                    false, () -> logger.error("ARTIFACT TAMPERED: {} | Expected: {} | Actual: {}", targetPath.getFileName(), expectedHash, actualHash),
                    true, () -> {}
                ).get(valid).run();
                
                return valid;
            })
            .orElseGet(() -> {
                logger.warn("Unsigned Artifact: No checksum found for {}", targetPath.getFileName());
                return false; // Fail-secure: unverified means invalid
            });
    }

    private static String calculateHash(final Path path) {
        final int hexMask = 0xff;
        return Try.of(() -> {
            final MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            final byte[] hashBytes = digest.digest(Files.readAllBytes(path));
            final StringBuilder hexString = new StringBuilder();
            
            java.util.stream.IntStream.range(0, hashBytes.length)
                .map(i -> hashBytes[i] & hexMask)
                .forEach(b -> {
                    final String hex = Integer.toHexString(b);
                    // Declarative padding
                    Map.<Boolean, Runnable>of(
                        true, () -> hexString.append('0'),
                        false, () -> {}
                    ).get(hex.length() == 1).run();
                    hexString.append(hex);
                });
            
            return hexString.toString();
        }).getOrElseThrow(e -> new RuntimeException("Hash calculation failed", e));
    }
}
