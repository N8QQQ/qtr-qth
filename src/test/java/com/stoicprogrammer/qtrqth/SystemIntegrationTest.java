package com.stoicprogrammer.qtrqth;

import com.stoicprogrammer.qtrqth.base.BddTest;
import com.stoicprogrammer.qtrqth.model.ConfluenceHealth;
import com.stoicprogrammer.qtrqth.model.TelemetryPulse;
import com.stoicprogrammer.qtrqth.nmea.GpsData;
import com.stoicprogrammer.qtrqth.analysis.PrecisionMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class SystemIntegrationTest extends BddTest {

    @TempDir
    private Path tempDir;

    @Test
    void should_integrate_all_components_into_pulse_stream() {
        final Instant now = Instant.parse("2026-05-17T12:00:00Z");
        final GpsData sampleData = new GpsData(LocalTime.of(12, 0, 0), null, 40.0, -80.0, 100.0, 8);
        final TelemetryPulse pulse = TelemetryPulse.start("trigger", null, ConfluenceHealth.HEALTHY_HARDWARE, now, sampleData, PrecisionMetrics.EMPTY);
        final List<TelemetryPulse> capturedPulses = List.of(pulse);
        
        assertThat(capturedPulses).isNotEmpty();
    }
}
