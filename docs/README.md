---
layout: default
title: README
---
# qtr-qth : GPS Time & Location Hub

[![Build Status](https://github.com/n8qqq/qtr-qth/actions/workflows/ci.yml/badge.svg)](https://github.com/n8qqq/qtr-qth/actions/workflows/ci.yml)
[![Security: CodeQL](https://github.com/n8qqq/qtr-qth/actions/workflows/codeql.yml/badge.svg)](https://github.com/n8qqq/qtr-qth/actions/workflows/codeql.yml)
[![Documentation](https://github.com/n8qqq/qtr-qth/actions/workflows/docs.yml/badge.svg)](https://n8qqq.github.io/qtr-qth/)
[![DOI](https://zenodo.org/badge/1200462693.svg)](https://doi.org/10.5281/zenodo.20114280)

`qtr-qth` is a cross-platform application for Amateur Radio operators.
 It synchronizes your system clock (QTR) and provides precise location data (QTH), including Maidenhead Grid Squares, using a standard GPS USB receiver.

## 🚀 Capabilities

- **Time Sync:** Precision UTC time extraction from GPS satellites.
- **Location Hub:** Real-time Latitude, Longitude, and Altitude monitoring.
- **Grid Square:** Automatic 6-character Maidenhead Grid Square calculation (e.g., `FN20xr`).
- **Sim Mode:** Built-in GPS simulator for testing away from the radio shack.
- **Observability:** Professional-grade logging with pulse-tracking for 24/7 reliability.

## 📑 Documentation
- **Architecture:** [ARCHITECTURE.md](architecture/ARCHITECTURE.md) - Deep dive into the "Two-River Confluence" model.
- **Blueprint:** [PLAN.md](roadmap/PLAN.md) - The tactical execution roadmap.
- **Flight Log:** [SESSIONS.md](roadmap/SESSIONS.md) - SBA velocity tracking and metrics.
- **Developer Guide:** [DEVELOPER.md](DEVELOPER.md) - Environment setup and release protocols.

## 🏃 Getting Started

### 1. Requirements
- **Java 21:** Ensure you have Java 21 installed on your system.
- **GPS Device:** (Optional) A USB GPS dongle (u-blox, VFAN, etc.).

### 2. Configuration
The application uses a `qtr-qth.properties` file. It will be created automatically on first run, but you can customize it as needed:

| Property | Default | Description |
| :--- | :--- | :--- |
| `simulation.mode` | `true` | Set to `false` to use real hardware. |
| `display.raw.telemetry` | `false` | Set to `true` to see raw NMEA data from the GPS. |
| `serial.baud` | `9600` | The speed of your GPS device (usually 9600). |
| `gps.discovery.keywords` | `gps,u-blox...` | Keywords to help find your GPS hardware. |

### 3. Running the Application
Download the latest release and run the script for your OS:

**Windows:** `./qtr-qth.bat` | **Linux / macOS:** `./qtr-qth`

## 📊 Logs & Observability
`qtr-qth` maintains two rotating log files in the `logs/` directory:
- **`qtr-qth.log`**: The "Shack Log." Contains high-level operational info.
- **`trace.log`**: The "Lab Log." Contains granular, second-by-second telemetry data.

**Dynamic Logging:** You can change log levels (e.g., from `INFO` to `DEBUG`) in real-time by editing `src/main/resources/logback.xml`. The application scans for changes every 30 seconds without needing a restart.

## 🔍 Troubleshooting
If your device is not detected, check the `logs/qtr-qth.log` file. For deep hardware debugging, set `simulation.mode=false` and check `logs/trace.log` to see every character arriving from the serial port.

## ⚖️ License & Copyright

Developed by **Nicholas R. Ustick (N8QQQ)** - [StoicProgrammer.com](https://StoicProgrammer.com)

Copyright (c) 2026 Nicholas R. Ustick. This project is licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for full details.

## 📝 Scientific Citation

If you use this software for research, technical papers, or formal radio experiment reports, please cite it using the metadata in the [CITATION.cff](CITATION.cff) file. This project is indexed for archival preservation on **Zenodo**.

*Engineered in collaboration with JARVIS (via Gemini CLI).*
