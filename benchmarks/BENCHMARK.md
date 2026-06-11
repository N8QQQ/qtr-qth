# Heritage Benchmark Registry: Master Comparison

This registry tracks high-fidelity performance metrics across all supported `qtr-qth` platforms.

## 🏆 Platform Leaderboard

| Platform | OS | Receiver | Baud | Avg Offset | RMS Jitter | NTP | Date |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Windows 11** | Win 11 Pro | u-blox 7 | 115,200 | ~-149.2ms | **~2.5ms** | Stratum 3 | 2026-06-08 |
| **Mechanar** | WSL2 (24.04) | u-blox 7 | 115,200 | ~-142.4ms | ~39.4ms | Stratum 2 | 2026-06-09 |
| **Linux Laptop** | Ubuntu 24.04 | u-blox 7 | 115,200 | ~63.5ms | ~70.2ms | Stratum 2 | 2026-06-05 |
| **Mechanar** | WSL2 (24.04) | u-blox 7 | 115,200 | ~-42.4ms | **~9.8ms** | Stratum 2 | 2026-06-10 |

---

## 📈 Analysis History

### Windows Workstation (Athena)
- **File**: `benchmarks/windows-workstation/20260608-athena-live-115k.nmea`
- **Context**: Native host performance.
- **Observations**: Exceptional precision (2.5ms jitter). Windows 11 serial interrupt handling is the current gold standard for this fleet.

### Mechanar (WSL2 on Athena)
- **File**: `benchmarks/mechanar/20260610-mechanar-live-115k.nmea`
- **Context**: Virtualized environment testing via usbipd.
- **Observations**: Final Phase 10 Certification. Jitter optimization (~9.8ms) achieved through reactive stream refinements and Logback 1.5.34 metadata sync. Stability confirmed over 30s hardware capture.

### Linux Laptop (Gandalf)
- **File**: `benchmarks/linux-laptop/20260605-ublox7-115k.nmea`
- **Context**: Initial baseline for native Linux performance.
- **Observations**: Highest jitter in the fleet (~70ms), potentially due to laptop power management or driver specifics.

### Raspberry Pi 4/5 (Stardock)
- **Status**: Future Target.
- **Focus**: Thermal impact on clock precision and serial interrupt latency.
