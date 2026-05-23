package com.stoicprogrammer.qtrqth.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Intelligent observer that pattern-matches NMEA sentence bursts to identify the 'Sentinel' trigger.
 * Enables zero-latency 1Hz synchronization across diverse hardware.
 */
public final class CalibrationEngine {
    private static final Logger logger = LoggerFactory.getLogger(CalibrationEngine.class);

    private final int requiredConfidence;
    private final int maxCycles;
    private List<String> currentBurst = Stream.<String>empty().collect(Collectors.toList());
    private final Map<List<String>, Integer> patterns = new ConcurrentHashMap<>();
    
    private String lastTimestamp = "";
    private List<String> lastCompleteBurst = List.of();
    private Optional<String> identifiedSentinel = Optional.empty();
    private int cyclesObserved = 0;
    private boolean timedOut = false;

    public CalibrationEngine(final int requiredConfidence, final int maxCycles) {
        this.requiredConfidence = requiredConfidence;
        this.maxCycles = maxCycles;
    }

    /**
     * Ingests a single sentence and its associated NMEA timestamp.
     * @param sentence The raw NMEA sentence.
     * @param timestamp The extracted UTC timestamp (e.g., 123456.00).
     * @return An Optional containing the identified Sentinel if confidence is reached.
     */
    public Optional<String> observe(final String sentence, final String timestamp) {
        if (identifiedSentinel.isPresent() || timedOut) {
            return identifiedSentinel;
        }

        final String type = sentence.split(",")[0];

        // Detect Rollover
        if (!timestamp.equals(lastTimestamp) && !lastTimestamp.isEmpty()) {
            cyclesObserved++;
            processBurstCompletion();
            
            if (cyclesObserved >= maxCycles && identifiedSentinel.isEmpty()) {
                this.timedOut = true;
                logger.warn("CALIBRATION TIMEOUT: Failed to identify stable hardware cadence after {} cycles.", maxCycles);
            }
        }

        lastTimestamp = timestamp;
        currentBurst.add(type);

        return identifiedSentinel;
    }

    private void processBurstCompletion() {
        final List<String> burstCopy = List.copyOf(currentBurst);
        currentBurst = Stream.<String>empty().collect(Collectors.toList());

        if (burstCopy.isEmpty()) return;

        logger.debug("Observed Burst Pattern [Cycle {}]: {}", cyclesObserved, burstCopy);

        // Pattern Matching
        if (burstCopy.equals(lastCompleteBurst)) {
            final int confidence = patterns.merge(burstCopy, 1, Integer::sum);
            logger.info("Calibration Confidence: {}/{}", confidence, requiredConfidence);

            if (confidence >= requiredConfidence) {
                this.identifiedSentinel = Optional.of(burstCopy.get(burstCopy.size() - 1));
                logger.info("PHASE LOCK ACHIEVED: Sentinel identified as {}", identifiedSentinel.get());
            }
        } else {
            if (cyclesObserved > 1) {
                logger.debug("Cadence Mismatch. Resetting confidence.");
                patterns.clear(); 
            }
        }

        lastCompleteBurst = burstCopy;
    }

    public boolean isCalibrated() {
        return identifiedSentinel.isPresent();
    }

    public boolean isTimedOut() {
        return timedOut;
    }

    public String getSentinel() {
        return identifiedSentinel.orElse("");
    }

    /**
     * Forces a specific sentinel, bypassing the calibration phase.
     * Used for loading cached Device Calibration Data.
     */
    public void forceSentinel(final String sentinel) {
        this.identifiedSentinel = Optional.of(sentinel);
        logger.info("PHASE LOCK FORCED: Using provided sentinel {}", sentinel);
    }
}
