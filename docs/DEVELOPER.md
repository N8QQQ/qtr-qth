---
layout: default
title: Developer Guide
---
# Developer Documentation: qtr-qth

This document outlines the technical environment, build workflows, and high-fidelity virtualization protocols for the `qtr-qth` project.

## 🏗️ Technical Architecture
For details on the functional pipeline, concurrency model, and system design, refer to the **[System Architecture (ARCHITECTURE.md)](architecture/ARCHITECTURE.md)**.

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
This project follows an **AI-first workflow** (Nicholas R. Ustick + JARVIS). All refactoring must follow the **"Heritage Grade"** standard: Zero mutable logic in core components and strictly decoupled hardware abstractions as defined in [`GEMINI.md`](GEMINI.md).

---

## 🛰️ Universal Orchestration (Docker Compose)
To ensure identical workflows across Windows, Linux, Mac, and RPi, all infrastructure tasks are managed via `docker-compose.yml`.

### 1. The Quality Gate (Local CI)
Certify the repository state against the official Ubuntu/Temurin environment:
```bash
docker-compose run --rm ci
```
*Mandate: Always use `--rm` to ensure a stateless lifecycle.*

### 2. The Documentation Hub (Staging)
Preview the rendered hub with p5.js flare and Mermaid diagrams:
```bash
docker-compose up docs
```
Access at: **http://localhost:4000/qtr-qth/** (Supports Live-Reload).

### 3. The Phantom Shack (Hardware Laboratory)
Boot a virtual Raspberry Pi environment with a spoofed GPS serial pipe:
```bash
docker-compose run --rm phantom
```
- **Virtual TTY:** `/dev/ttyUSB99` (fed by `gps_sim.nmea`).
- **Live Coordinator:** Send host data to the container via TCP port `9999`.
- **Hardware Pass-through:** Maps `/dev/ttyUSB0` (Linux/Pi hosts only).

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
