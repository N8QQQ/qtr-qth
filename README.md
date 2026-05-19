# qtr-qth : GPS Time & Location Hub

[![Build Status](https://github.com/n8qqq/qtr-qth/actions/workflows/ci.yml/badge.svg)](https://github.com/n8qqq/qtr-qth/actions/workflows/ci.yml)
[![Security: CodeQL](https://github.com/n8qqq/qtr-qth/actions/workflows/codeql.yml/badge.svg)](https://github.com/n8qqq/qtr-qth/actions/workflows/codeql.yml)
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
- **Heritage Grade Observability:** Professional pulse-tracking and dual-channel logging for 24/7 reliability.

## 📑 Technical Documentation
*The complete technical manual is hosted at the **[Documentation Hub](https://n8qqq.github.io/qtr-qth/)**.*

- **Architecture:** [The Two-River Confluence Model](docs/architecture/ARCHITECTURE.md)
- **Developer Guide:** [Environment & Release Protocols](docs/DEVELOPER.md)
- **Blueprint:** [Project Execution Roadmap](docs/roadmap/PLAN.md)
- **Flight Log:** [Session-Based Agile Metrics](docs/roadmap/SESSIONS.md)

---

## 🏃 Quick Start

### 1. Requirements
- **Java 21 LTS** (Eclipse Temurin preferred).
- **USB GPS Device** (u-blox, Prolific, or Silicon Labs based).

### 2. Launch
Download the latest release and run the script for your environment:

**Windows:**
```powershell
./qtr-qth.bat
```

**Linux / RPi:**
```bash
chmod +x qtr-qth && ./qtr-qth
```

### 3. Virtualization (Developer Staging)
Launch the **Phantom Shack** lab using Docker:
```bash
docker-compose run --rm phantom
```

---

## ⚖️ License & Identity

- **Developer:** Nicholas R. Ustick ([N8QQQ](https://www.qrz.com/db/N8QQQ)) - [StoicProgrammer.com](https://StoicProgrammer.com)
- **Affiliation:** Stoic Programmer
- **License:** [GNU GPL v3.0](LICENSE)

## 📝 Scientific Citation
This project is indexed for archival preservation on **Zenodo**. If you use this software in research or technical publications, please cite it:

> Ustick, N. R. (2026). qtr-qth: High-Precision GPS Time & Location Hub (Version 0.4.4). Zenodo. https://doi.org/10.5281/zenodo.20257593

---
*Engineered in collaboration with JARVIS (via Gemini CLI).*
