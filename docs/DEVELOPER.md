---
layout: default
title: Developer Guide
---
# Developer Documentation: qtr-qth

This document outlines the technical environment, build workflows, and high-fidelity virtualization protocols for the `qtr-qth` project.

## 🏗️ Technical Architecture
For details on the functional pipeline, concurrency model, and system design, refer to the **[System Architecture (ARCHITECTURE.md)](architecture/ARCHITECTURE.md)**.

### The Pulse Lifecycle (Sequence View)
The following diagram traces a single NMEA sentence from the hardware edge through its transformation into a high-fidelity telemetry pulse.

```mermaid
sequenceDiagram
    participant HW as GPS Hardware
    participant SC as SerialConnector
    participant IG as Ingress Guard (Producer)
    participant EQ as Event Queue
    participant RM as Reactive Monolith (Consumer)
    participant NP as NmeaParser

    HW->>SC: Byte Stream
    SC->>IG: Capture T1 Edge Stamp
    IG->>EQ: push TelemetryEvent(sentence, T1)
    EQ->>RM: poll event
    RM->>NP: parse(sentence, previous)
    NP-->>RM: Enriched GpsData
    RM->>RM: Trigger TelemetryPulse
    RM->>Consumer: TelemetryPulse (Pulse ID + T1)
```

## 🛠️ Development Workflow

### Prerequisites
- **Java:** JDK 21 LTS (Eclipse Temurin preferred).
- **Docker & Docker Compose:** Required for virtualization and CI mirroring.
- **Gradle:** 9.3.0 (Utilized via the included `./gradlew` wrapper).

### Local Execution (Host)
- `./gradlew run`: Start the application.
- `./gradlew installDist`: Create unzipped distribution in `build/install/qtr-qth/`.
- `./gradlew test`: Execute the BDD and unit test suites.
- `./gradlew probeHardware`: Scans the host system for physical/virtual GPS serial hardware.

## 🤖 AI-Integrated Development
This project follows an **AI-first workflow** (Nicholas R. Ustick + JARVIS). All refactoring must follow the **"Heritage Grade"** standard: Zero mutable logic in core components and strictly decoupled hardware abstractions as defined in the project's root `GEMINI.md` mandates.

---
## 🛰️ Infrastructure Ecosystem (Docker)
To ensure high-fidelity development and cross-platform parity, `qtr-qth` utilizes a multi-container orchestration strategy. This eliminates platform-specific "crutch" scripts and ensures deterministic hardware testing across Windows, Linux, and macOS.

### Container Catalog & Utility

| Service | Role | Key Capability | Resource Envelope |
| :--- | :--- | :--- | :--- |
| **`ci`** | Quality Gate | Headless compilation, linting, and testing. | Native Host |
| **`docs`** | Staging Hub | Real-time preview of the Jekyll-based manual. | 512MB RAM |
| **`phantom`** | Virtual Shack | Hardware spoofing via `socat` and TCP bridges. | 1.0 CPU / 1GB RAM |
| **`stress-test`** | ARM64 Mirror | Emulated ARM64 execution to certify Pi-parity. | 0.5 CPU / 512MB RAM |

---

### 🛠️ Universal Orchestration Commands

The following commands are platform-agnostic and should be used instead of any legacy PowerShell or Bash scripts for host-level orchestration.

#### 1. The Quality Gate (`ci`)
- **Command:** `docker-compose run --rm ci`
- **Purpose:** Mirrors the GitHub Actions environment. It executes a full "Clean-Check-Test" cycle.

#### 2. The Documentation Hub (`docs`)
- **Command:** `docker-compose up docs`
- **Purpose:** Real-time preview of the documentation at `http://localhost:4000/qtr-qth/`.
- **Note:** To change the Jekyll theme, modify `remote_theme` in `docs/_config.yml`.

#### 3. The Phantom Shack (`phantom`)
- **Command:** `docker-compose run --rm phantom`
- **Purpose:** A virtualized laboratory for hardware logic and spoofing.
- **TCP Bridge:** Connect your host's NMEA stream to `localhost:9999`.

#### 4. Maintenance & Cleanup (The Scrub)
- **Command:** `docker-compose down -v --remove-orphans`
- **Purpose:** Neutralize the environment and reclaim host resources.
- **Protocol:** Run this whenever switching between hardware and simulation modes, or if `socat` handles become stale.

---
## 🔍 Debugging & Troubleshooting
Given the hardware-dependent nature of `qtr-qth`, debugging often requires looking beyond the Java stack into the OS kernel and virtualized pipes.

### 1. Serial Hardware Diagnostics
If the application fails to identify your GPS receiver:
- **Run the Probe:** Use `./gradlew probeHardware` to see exactly what the OS reports.
- **Permission Check (Linux):** Ensure your user is in the `dialout` group: `sudo usermod -a -G dialout $USER`.
- **Lock Contention:** Ensure no other tool (e.g., GPSD, Minicom, PuTTY) is holding the port open.

### 2. Virtualization & Phantom Shack Issues
If the `phantom` container behaves unexpectedly:
- **Stale TTY Handles:** If `socat` fails to create `/dev/ttyUSB99`, ensure a previous container didn't leave a ghost handle. Run **The Scrub** routine.
- **Live Stream Verification:** To verify the TCP bridge, send a manual sentence from your host: 
  `echo "$GPZDA,123456,01,01,2026,00,00*6C" | nc localhost 9999`
- **Architecture Mismatch:** If `stress-test` fails to start, verify that Docker Desktop has **QEMU support** enabled for ARM64 emulation.

### 3. Telemetry Pulse Analysis
The system utilizes **Mapped Diagnostic Context (MDC)** to track data through the pipeline.
- **Pulse ID Tracking:** Every log line is prefixed with a 4-digit Hex ID (e.g., `[B026]`). Use this to correlate raw NMEA ingestion with final parsed results.
- **Health Signaling:** Check the `[REACTIVE_LOCK | GPS: Status | NTP: Status | Mode: Status]` prefix in the logs.
    - `REACTIVE_LOCK`: Zero-latency event trigger achieved.
    - `GPS: RECOVERY`: System is re-scanning for hardware after signal loss.
    - `GPS: ACTIVE`: The GPS data stream is flowing normally.
- **Raw Telemetry:** Enable `display.raw.telemetry=true` in `qtr-qth.properties` to see the unprocessed byte-stream in the logs.

---
## 🚀 Heritage Release Protocol

Every release must follow this high-fidelity sequence:

### 1. Pre-Release Metadata Sync
- [ ] Bump version in `build.gradle.kts`.
- [ ] Update `version` and `date-released` in `CITATION.cff`.
- [ ] **Roadmap Alignment:** Synchronize `PLAN.md` with the current tactical state.

### 2. Logic & Security Certification (DoD)
- [ ] **Test Pass:** Run `docker-compose run --rm ci`. **All tests must be green.**
- [ ] **OWASP 2025 Audit:** Manual/Static scan for Supply Chain and Integrity vulnerabilities.

### 3. Distribution & Archival
- [ ] **Artifact Generation:** Run `./gradlew distZip`.
- [ ] **Verification:** Confirm signed git tag `v[version]` and attach ZIP to the GitHub Release.
- [ ] **Scholarly Sync:** Ensure Zenodo DOI metadata (Affiliation, Keywords) matches the repo.

---
*For end-user instructions, see [README.md](README.md).*.
