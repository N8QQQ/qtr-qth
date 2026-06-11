package com.stoicprogrammer.qtrqth.ntp;

import java.time.Instant;

/**
 * Rich data carrier for NTP polling results.
 * Encapsulates time along with precision and quality metadata.
 */
public record NtpResponse(
    Instant time, 
    long rttMilliseconds, 
    int stratum, 
    double rootDispersionMilliseconds
) {}
