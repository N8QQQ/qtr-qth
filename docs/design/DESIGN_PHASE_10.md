# Phase 10 Design: Precision Drift & Offset Analysis (v0.8.0)

**Objective:** Quantify the system clock's "Resolving Power" by calculating the microsecond-level drift against authoritative GPS and NTP sources.

## 🏛️ Precision Analytics Architecture

With the **Phase-Locked Logic (Phase 9)** providing a stable 1Hz heartbeat, we can now perform high-fidelity statistical analysis on the "Fast River" (GPS) and "Slow River" (NTP) confluence.

### 1. The Offset Engine
The engine calculates a **Drift Vector** for every telemetry pulse:
*   **$Offset_{GPS}$:** `ingressTime` (System) - `utcTime` (GPS Authority).
*   **$Offset_{NTP}$:** `ingressTime` (System) - `referenceTime` (NTP Authority).
*   **$Epsilon$:** The calculated effective accuracy (Resolving Power) for the current host OS.

### 2. Signal Intelligence (Constellation Tracking)
To provide context for the drift, we expand the NMEA ingestion to include signal-quality metrics:
*   **GSV Ingestion:** Capture per-satellite SNR, Azimuth, and Elevation.
*   **GSA Ingestion:** Capture Dilution of Precision (PDOP, HDOP, VDOP) and identifying "Active" satellites.

### 3. Statistical Window (The Jitter-Bug)
The system maintains a functional sliding buffer of the last 100 pulses to calculate:
*   **RMS Jitter:** Root Mean Square error of the timing signal.
*   **Stability:** Standard Deviation of the offset over the window.

## 🛠️ Data Model Evolution

### TelemetryPulse (Enriched)
The pulse now carries the **Signal Matrix**:
- `systemOffset`: Microsecond delta.
- `rmsJitter`: Statistical noise.
- `satellites`: List of `SatelliteFix` records (ID, SNR, Position).
- `dop`: Record containing precision multipliers.

## 🔭 Visual Roadmap Integration
The data captured in this phase is designed specifically to feed the **Phase 15 (Visual Shack)** charts:
1.  **Temporal Drift Plot:** Dual-trace offset history.
2.  **Polar Sky Plot:** Celestial satellite map.
3.  **SNR Matrix:** Signal strength bar charts.
4.  **Jitter Histogram:** Statistical distribution.
