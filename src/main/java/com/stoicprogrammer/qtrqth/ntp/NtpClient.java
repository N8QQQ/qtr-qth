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
     * Polls the specified NTP server for high-fidelity time and metadata.
     * @param hostname The NTP server address.
     * @return An Optional containing the NtpResponse, or empty if the poll failed.
     */
    public Optional<NtpResponse> pollDetailed(String hostname) {
        NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(timeoutMs);
        
        try {
            client.open();
            InetAddress hostAddr = InetAddress.getByName(hostname);
            
            // Capture Start Time for RTT calculation
            TimeInfo info = client.getTime(hostAddr);
            info.computeDetails(); // Computes RTT and Offset
            
            Instant networkTime = Instant.ofEpochMilli(info.getMessage().getTransmitTimeStamp().getTime());
            long rtt = (info.getDelay() != null) ? info.getDelay() : -1;
            int stratum = info.getMessage().getStratum();
            double dispersion = info.getMessage().getRootDispersionInMillisDouble();

            NtpResponse response = new NtpResponse(networkTime, rtt, stratum, dispersion);
            
            logger.debug("NTP Detailed Poll Successful: {} | RTT: {}ms | Stratum: {} | Dispersion: {}ms", 
                hostname, rtt, stratum, String.format("%.2f", dispersion));
            
            return Optional.of(response);
            
        } catch (Exception e) {
            logger.warn("NTP Detailed Poll Failed for {}: {}", hostname, e.getMessage());
            return Optional.empty();
        } finally {
            if (client.isOpen()) {
                client.close();
            }
        }
    }

    /**
     * Polls the specified NTP server for the current time.
     * @param hostname The NTP server address (e.g., pool.ntp.org).
     * @return An Optional containing the Instant, or empty if the poll failed.
     */
    public Optional<Instant> poll(String hostname) {
        return pollDetailed(hostname).map(NtpResponse::time);
    }
}
