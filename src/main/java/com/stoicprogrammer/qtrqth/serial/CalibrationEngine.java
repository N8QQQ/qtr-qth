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
    private String lastCompleteBurstSentinel = "";
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

        // Detect Rollover
        if (isRollover(timestamp)) {
            handleRollover();
        }

        lastTimestamp = timestamp;
        currentBurst.add(sentence.split(",")[0]);

        return identifiedSentinel;
    }

    private boolean isRollover(final String timestamp) {
        return !timestamp.isEmpty() && !timestamp.equals(lastTimestamp) && !lastTimestamp.isEmpty();
    }

    private void handleRollover() {
        processBurstCompletion();
        if (cyclesObserved >= maxCycles && identifiedSentinel.isEmpty()) {
            this.timedOut = true;
            logger.warn("CALIBRATION TIMEOUT: Failed to identify stable hardware cadence after {} cycles.", maxCycles);
        }
    }

    private void processBurstCompletion() {
        if (currentBurst.isEmpty()) return;

        final String lastInBurst = currentBurst.get(currentBurst.size() - 1);
        currentBurst = Stream.<String>empty().collect(Collectors.toList());

        cyclesObserved++;
        logger.debug("Observed Burst End [Cycle {}]: {}", cyclesObserved, lastInBurst);

        // Sentinel Consistency Check: Is the last sentence before rollover always the same?
        if (lastInBurst.equals(lastCompleteBurstSentinel)) {
            final int confidence = patterns.merge(List.of(lastInBurst), 1, Integer::sum);
            logger.info("Calibration Confidence: {}/{}", confidence, requiredConfidence);

            if (confidence >= requiredConfidence) {
                this.identifiedSentinel = Optional.of(lastInBurst);
                logger.info("PHASE LOCK ACHIEVED: Sentinel identified as {}", identifiedSentinel.get());
            }
        } else {
            resetConfidence(lastInBurst);
        }

        lastCompleteBurstSentinel = lastInBurst;
    }

    private void resetConfidence(final String lastInBurst) {
        if (cyclesObserved > 1) {
            logger.debug("Sentinel Mismatch ({} vs {}). Resetting confidence.", lastInBurst, lastCompleteBurstSentinel);
            patterns.clear(); 
        }
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
