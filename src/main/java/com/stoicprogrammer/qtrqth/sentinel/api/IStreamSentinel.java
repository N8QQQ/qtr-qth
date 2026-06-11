package com.stoicprogrammer.qtrqth.sentinel.api;

import java.util.concurrent.TimeUnit;

/**
 * Pluggable strategy for system heartbeats and stream watchdogs.
 * Decouples temporal execution from business logic to support thermal optimization (RPi).
 */
public interface IStreamSentinel {
    /**
     * Starts the periodic execution of a task.
     * @param task The task to execute.
     * @param interval The period between executions.
     * @param unit The time unit for the interval.
     */
    void start(Runnable task, long interval, TimeUnit unit);

    /**
     * Gracefully stops the periodic execution and releases resources.
     */
    void stop();

    /**
     * Signals that an ingress event has occurred (resets watchdog timers).
     */
    default void notifyIngress() {
        // Optional implementation for watchdog strategies
    }
}
