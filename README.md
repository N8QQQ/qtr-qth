# qtr-qth : GPS Time & Location Hub

[![Build Status](https://github.com/n8qqq/qtr-qth/actions/workflows/ci.yml/badge.svg)](https://github.com/n8qqq/qtr-qth/actions/workflows/ci.yml)
[![Security: CodeQL](https://github.com/n8qqq/qtr-qth/actions/workflows/codeql.yml/badge.svg)](https://github.com/n8qqq/codeql.yml)
[![Documentation](https://github.com/n8qqq/qtr-qth/actions/workflows/docs.yml/badge.svg)](https://n8qqq.github.io/qtr-qth/)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.20257593.svg)](https://doi.org/10.5281/zenodo.20257593)
[![ORCiD](https://img.shields.io/badge/ORCiD-0009--0001--9211--8000-A6CE39?logo=orcid&logoColor=white)](https://orcid.org/0009-0001-9211-8000)

`qtr-qth` is a high-precision, branchless functional application designed for the Amateur Radio shack and scientific synchronization. It transforms a standard USB GPS receiver into a **Stratum 1 Time Authority** and real-time **Maidenhead Grid Square** telemetry hub.

---

## 🚀 Key Capabilities

- **Stratum 1 Precision:** High-fidelity UTC time extraction with micro-drift monitoring.
- **Location Telemetry:** Real-time Latitude, Longitude, Altitude, and Maidenhead Grid Square (6-character resolution).
- **Functional Confluence:** A unique "Two-River" model merging GPS and NTP data streams with zero mutable state.
- **Hardware Virtualization:** Integrated "Phantom Shack" for high-fidelity simulation on any OS (including ARM64 Pi-parity).
- **Self-Healing Recovery:** Automatic hardware re-acquisition (Connection Neutralization) during signal loss.

---

## 📑 Documentation Hub
*The complete technical manual is hosted at the **[Documentation Hub](https://n8qqq.github.io/qtr-qth/)**.*

- **Architecture:** [The Two-River Confluence Model](docs/architecture/ARCHITECTURE.md)
- **Developer Guide:** [Environment & Release Protocols](docs/DEVELOPER.md)
- **Manual Test Plan:** [End-to-End Verification](docs/MANUAL_TEST_PLAN.md)
- **Blueprint:** [Project Execution Roadmap](docs/roadmap/PLAN.md)

---

## 🏃 Quick Start

### 1. Requirements
- **Java 21 LTS** (Eclipse Temurin preferred).
- **USB GPS Device:** Compatible with NMEA 0183 (u-blox, Prolific, Silicon Labs, etc.).

### 2. Installation & Run
Download the latest release and run the script for your environment:

**Windows:**
```powershell
./qtr-qth.bat
```

**Linux / Raspberry Pi:**
```bash
chmod +x qtr-qth && ./qtr-qth
```

### 3. Hardware Discovery Probe
If you are unsure which port your GPS is using, run the integrated discovery probe:
**Windows:** `./qtr-qth.bat --probe`
**Linux / RPi:** `./qtr-qth --probe`

### 4. Environment Doctor
If you are experiencing issues starting the application, run the environment doctor to diagnose your system:
**Windows:** `./qtr-qth.bat --doctor`
**Linux / RPi:** `./qtr-qth --doctor`

### 5. Configuration
Upon first run, the application creates `qtr-qth.properties` in the execution directory. You can tune the following parameters:
- `serial.baud`: Default `9600`. Match your GPS receiver's baud rate.
- `ntp.server`: Comma-separated list of NTP servers for drift verification.
- `display.raw.telemetry`: Set to `true` to see raw NMEA sentences in the logs.
- `simulation.mode`: Set to `true` to run without hardware using built-in samples.
- `sync.threshold.ms`: Maximum allowed delta between sources before a sync warning.

---

## 📡 Understanding the Pulse
The application outputs a high-fidelity telemetry pulse every second. Each pulse is tracked via a unique **Pulse ID** (e.g., `[B026]`).

**Example Output:**
`[REACTIVE_LOCK | GPS:ACTIVE | NTP:ACTIVE | Mode:HARDWARE_LOCK] Fix: UTC: 12:34:56 | Date: 2026-05-24 | Lat: 45.12340 | Lon: -87.56780 | Alt: 100.0m | Sats: 8 | HDOP: 1.00 | NTP: 2026-05-24T12:34:56.012Z (RTT: 15ms) | Grid: EN66bg`

- **GPS Status:** `ACTIVE` (normal), `RECOVERY` (searching for hardware), `OFFLINE` (disabled).
- **NTP Status:** Displays the latest network time reference and Round Trip Time (RTT).
- **Grid:** Your current Maidenhead Grid Square, recalculated every second.

---

## 🛠️ Hardware Compatibility
`qtr-qth` is designed to be plug-and-play. It applies fuzzy-matching logic to auto-discover GPS receivers on your USB ports. It is certified against:
- GlobalSat BU-353-S4
- u-blox 6/7/8/9 Series
- Generic G-Mouse USB Receivers

---

## ⚖️ License & Identity
- **Developer:** Nicholas R. Ustick ([N8QQQ](https://www.qrz.com/db/N8QQQ))
- **License:** [GNU GPL v3.0](LICENSE)

---
*Engineered in collaboration with JARVIS (via Gemini CLI).*
