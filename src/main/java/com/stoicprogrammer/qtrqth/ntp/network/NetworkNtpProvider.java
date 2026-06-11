package com.stoicprogrammer.qtrqth.ntp.network;

import com.stoicprogrammer.qtrqth.ntp.NtpResponse;
import com.stoicprogrammer.qtrqth.ntp.api.INtpProvider;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Optional;

/**
 * Production implementation of INtpProvider using Apache Commons Net.
 * Adheres to strict finality and expression-based resource management.
 */
public final class NetworkNtpProvider implements INtpProvider {
    private static final Logger logger = LoggerFactory.getLogger(NetworkNtpProvider.class);

    @Override
    public Optional<NtpResponse> getTime(final String hostname, final int timeoutMilliseconds) {
        final NTPUDPClient client = new NTPUDPClient();
        client.setDefaultTimeout(timeoutMilliseconds);
        
        try {
            client.open();
            final InetAddress hostAddr = InetAddress.getByName(hostname);
            
            final TimeInfo info = client.getTime(hostAddr);
            info.computeDetails();
            
            final Instant networkTime = Instant.ofEpochMilli(info.getMessage().getTransmitTimeStamp().getTime());
            final long rtt = Optional.ofNullable(info.getDelay()).orElse(-1L);
            final int stratum = info.getMessage().getStratum();
            final double dispersion = info.getMessage().getRootDispersionInMillisDouble();

            return Optional.of(new NtpResponse(networkTime, rtt, stratum, dispersion));
            
        } catch (final Exception e) {
            logger.warn("NTP Network Poll Failed for {}: {}", hostname, e.getMessage());
            return Optional.empty();
        } finally {
            Optional.of(client)
                .filter(NTPUDPClient::isOpen)
                .ifPresent(NTPUDPClient::close);
        }
    }
}
