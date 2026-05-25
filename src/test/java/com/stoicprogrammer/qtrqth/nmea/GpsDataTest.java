package com.stoicprogrammer.qtrqth.nmea;

import com.stoicprogrammer.qtrqth.base.BddTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GpsDataTest extends BddTest {

    private static final int MOCK_HOUR = 12;
    private static final int MOCK_YEAR = 2026;
    private static final int MOCK_MONTH = 5;
    private static final int MOCK_DAY = 23;
    private static final double MOCK_LAT = 46.17005;
    private static final double MOCK_LON = -87.53281;
    private static final double MOCK_ALT = 425.1;
    private static final int MOCK_SATS = 12;
    private static final double MOCK_SPEED = 0.5;
    private static final double MOCK_TRACK = 125.5;
    private static final double MOCK_HDOP = 0.85;
    private static final double MOCK_VDOP = 1.2;
    private static final double MOCK_PDOP = 1.5;

    private final GpsFixture fixture = new GpsFixture();

    @Test
    void should_create_immutable_record_with_high_fidelity_telemetry() {
        final List<GpsData.SatelliteFix> sats = List.of(new GpsData.SatelliteFix(1, 45, 180, 35));
        final GpsData.PrecisionStats stats = new GpsData.PrecisionStats(1.2, 1.5, 2.0);
        
        fixture.given_full_parameters(
            LocalTime.of(MOCK_HOUR, 0, 0),
            LocalDate.of(MOCK_YEAR, MOCK_MONTH, MOCK_DAY),
            MOCK_LAT,
            MOCK_LON,
            MOCK_ALT,
            MOCK_SATS,
            MOCK_SPEED,
            MOCK_TRACK,
            MOCK_HDOP,
            MOCK_VDOP,
            MOCK_PDOP,
            "TEST_DIAG",
            sats,
            stats
        );
        fixture.then_high_fidelity_data_is_accurate(sats, stats);
    }

    private static final class GpsFixture {
        private GpsData data;

        void given_fix_parameters(final LocalTime time, final LocalDate date, final double lat, final double lon, final double alt, final int sats) {
            this.data = new GpsData(time, date, lat, lon, alt, sats);
        }

        void given_full_parameters(final LocalTime time, final LocalDate date, final double lat, final double lon, final double alt, final int sats, 
                                 final double speed, final double track, final double hdop, final double vdop, final double pdop, 
                                 final String diag, final List<GpsData.SatelliteFix> satList, final GpsData.PrecisionStats stats) {
            this.data = new GpsData(time, date, lat, lon, alt, sats, speed, track, hdop, vdop, pdop, diag, satList, stats);
        }

        void then_record_is_accurate() {
            assertThat(data.utcTime()).isEqualTo(LocalTime.of(MOCK_HOUR, 0, 0));
            assertThat(data.latitude()).isEqualTo(MOCK_LAT);
            assertThat(data.longitude()).isEqualTo(MOCK_LON);
            assertThat(data.satelliteCount()).isEqualTo(MOCK_SATS);
        }

        void then_high_fidelity_data_is_accurate(final List<GpsData.SatelliteFix> sats, final GpsData.PrecisionStats stats) {
            assertThat(data.speedKnots()).isEqualTo(MOCK_SPEED);
            assertThat(data.trackTrue()).isEqualTo(MOCK_TRACK);
            assertThat(data.hdop()).isEqualTo(MOCK_HDOP);
            assertThat(data.vdop()).isEqualTo(MOCK_VDOP);
            assertThat(data.pdop()).isEqualTo(MOCK_PDOP);
            assertThat(data.latestDiagnostic()).isEqualTo("TEST_DIAG");
            assertThat(data.satellitesInView()).isEqualTo(sats);
            assertThat(data.precisionStats()).isEqualTo(stats);
        }
    }
}
