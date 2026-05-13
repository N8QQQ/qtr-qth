package com.stoicprogrammer.qtrqth.ntp;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Optional;

/**
 * Robust NTP Client for capturing network-based time references.
 * Handles server polling and precision metadata extraction.
 */
public class NtpClient {
    private static final Logger logger = LoggerFactory.getLogger(NtpClient.class);
    private final int timeoutMs;

    public NtpClient(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    /**
     * Polls the specified NTP server for the current time.
     * @param hostname The NTP server address (e.g., pool.ntp.org).
     * @return An Optional containing the Instant, or empty if the poll failed.
     */
    public Optional<Instant> poll(String hostname) {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(timeoutMs);
        
        try {
            client.open();
            InetAddress hostAddr = InetAddress.getByName(hostname);
            TimeInfo info = client.getTime(hostAddr);
            
            // Extract the transmit timestamp (the time the server sent the response)
            long transmitTime = info.getMessage().getTransmitTimeStamp().getTime();
            Instant networkTime = Instant.ofEpochMilli(transmitTime);
            
            logger.debug("NTP Poll Successful: {} -> {}", hostname, networkTime);
            return Optional.of(networkTime);
            
        } catch (Exception e) {
            logger.warn("NTP Poll Failed for {}: {}", hostname, e.getMessage());
            return Optional.empty();
        } finally {
            if (client.isOpen()) {
                client.close();
            }
        }
    }
}
