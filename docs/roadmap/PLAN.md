---
layout: default
title: Project Roadmap
---
# Project Blueprint: qtr-qth

`qtr-qth` is a high-precision GPS Time (QTR) and Location (QTH) hub designed for mission-critical synchronization in Amateur Radio and technical shacks.

## 🏛️ Technical Design
For details on the concurrency model, functional pipeline, and architectural decisions, see [ARCHITECTURE.md](../architecture/ARCHITECTURE.md).

---

## 🗺️ Execution Roadmap

### Phase 1: Foundation (COMPLETED - v0.1.0)
- [x] **Phase 1.1: Project Skeleton (Gradle/Java 21)**
- [x] **Phase 1.2: NMEA Sentence Capture**
- [x] **Phase 1.3: GPS Data Records (Immutable)**
- [x] **Phase 1.4: Grid Square Calculator**

### Phase 2: Serial Plumbing (COMPLETED - v0.2.0)
- [x] **Phase 2.1: jSerialComm Integration**
- [x] **Phase 2.2: Hardware Abstraction Layer (ISerialProvider)**
- [x] **Phase 2.3: Port Auto-Discovery**
- [x] **Phase 2.4: Data Streaming Pipeline**

### Phase 3: Observability (COMPLETED - v0.2.0)
- [x] **Phase 3.1: Logback Integration**
- [x] **Phase 3.2: Mapped Diagnostic Context (MDC) for Pulse ID**
- [x] **Phase 3.3: High-Fidelity Console UI**

### Phase 4: Network Time Reference (COMPLETED - v0.3.0)
- [x] **Phase 4.1: Supply Chain & Console UX**
- [x] **Phase 4.2: Baseline NTP Client**
- [x] **Phase 4.3: Precision Metadata**
- [x] **Phase 4.4: Pipeline Integration**
- [x] **Phase 4.5: Resilience & Multi-Pooling**
- [x] **Phase 4.6: Test HAL & CI Stabilization**

### Phase 5: The Functional Blueprint (COMPLETED - v0.4.0)
- [x] **Phase 5.1: Functional Purity Refactor**
- [x] **Phase 5.2: Immutability Enforcement**
- [x] **Phase 5.3: Automated Linter Integration**
- [x] **Phase 5.4: Test Suite Reconciliation**

### Phase 6: Structural Hardening (COMPLETED - v0.4.3)
**Objective:** Final polish of the functional baseline before entering complex telemetry analysis.
**Branch:** `feat/galvanic-ground-plane`
- [x] **Phase 6.1: Typed Configuration Pipeline:** Parse properties into an immutable `AppConfig` record to eliminate Stringly-Typed logic.
- [x] **Phase 6.2: Numeric Purity:** Wrap parsing mechanisms (like `Double.parseDouble`) in `Optional` wrappers via `Functional.java` to prevent hardware noise exceptions.
- [x] **Phase 6.3: Supply Chain Synchronization:** Refine `verification-metadata.xml` to include multi-platform checksums and trust-keys (Windows/Linux/ARM).
- [x] **Phase 6.4: CI Action Refresh:** Update `gradle/actions/wrapper-validation` to latest major version to silence Node.js 20 deprecation.
- [x] **Phase 6.5: Performance Certification (JMH & Vavr):** Implement the Java Microbenchmark Harness to certify nanosecond-level stability of hot paths.
- [x] **Phase 6.6: The Test Data Vault:** Externalize all raw NMEA test literals into `/src/test/resources/telemetry` for high-fidelity BDD.
- [x] **Phase 6.7: Zenodo Metadata Weaving:** Implement `.zenodo.json` to ensure automated high-fidelity versioned citations.
- [x] **Phase 6.8: Logic Consolidation:** Perform a codebase-wide sweep to eliminate duplicate parsing logic and optimize functional pipelines.

### Phase 7: The Virtual Shack (v0.5.0) - COMPLETED
**Objective:** Cross-platform simulation of Linux/ARM64 and physical hardware.
**Branch:** `feat/phantom-shack-logic`
- [x] **Phase 7.1: Universal Orchestration:** Implemented `docker-compose.yml` for platform-agnostic development.
- [x] **Phase 7.2: Hardware Spoofing (socat):** Certified virtual TTY (`/dev/ttyUSB99`) with NMEA background streaming.
- [x] **Phase 7.3: Graceful Hardware Fallback:** Refactor `SystemOrchestrator` to prioritize hardware mode; implement auto-failover to `SimulationSerialProvider` if discovery fails.
- [x] **Phase 7.4: Multi-Arch & Resource Hardening:** Certified ARM64 Pi-parity and enforced hardware-accurate resource constraints.
- [x] **Phase 7.5: Live GPS Coordinator:** Implemented TCP bridge (Port 9999) for host-to-container hardware streaming.
- [x] **Phase 7.6: Infrastructure Decommissioning:** Codified 'The Scrub' routine and removed legacy PowerShell scripts.
- [x] **Phase 7.7: Runtime Resilience:** Resolved Defect 7.7.A (Restoration Stall) via Connection Neutralization pattern and Watchdog monitors.
- [x] **Phase 7.8: Repository Branding:** Implemented p5.js 'Constellation' branding engine (1280x640px).
- [x] **Phase 7.9: Project Monitoring & Traffic Analysis:** Established protocol for auditing GitHub traffic to distinguish adoption from automation.
- [x] **Phase 7.10: The Environment Doctor:** Implemented `--doctor` diagnostic suite with prescriptive rescue instructions.

### Phase 8: Temporal Virtualization (v0.6.0) - COMPLETED
**Objective:** Decouple time-awareness for deterministic math verification.
**Branch:** `feat/chrono-tesseract`
- [x] **Phase 8.1: Architectural Design:** Define the "Orchestrator-Centric Edge Stamping" model.
- [x] **Phase 8.2: Core Model Evolution:** Add `ingressTime` to `TelemetryPulse` record.
- [x] **Phase 8.3: Clock Dependency Injection:** Inject `InstantSource` into `SystemOrchestrator`.
- [x] **Phase 8.4: Simulation Refactor:** Cleanse `SimulationNtpProvider` of `Instant.now()`.
- [x] **Phase 8.5: Hermetic Test Certification:** Verify jitter/drift math using `Clock.fixed()`.

### Phase 9: Reactive State Synchronization (v0.7.0) - COMPLETED (May 25, 2026)
**Objective:** Achieve Phase Lock via stateless, zero-latency ingestion of the 1Hz telemetry stream.
**Branch:** `feat/phase-locked-logic`
- [x] **Phase 9.1: Reactive Ingestion Pipeline:** Refactor `SystemOrchestrator` to process NMEA sentences as discrete asynchronous events.
- [x] **Phase 9.2: High-Fidelity Edge Stamping:** Capture high-resolution system timestamps at the exact microsecond of sentence validation.
- [x] **Phase 9.3: Technical Purity Sweep:** Elimination of all imperative `if/else` and `for` loops in core telemetry logic.
- [x] **Phase 9.4: High-Fidelity Stress Certification:** Certified at 50Hz (921,600 baud equivalent) with 0.0ms average lag.
- [x] **Phase 9.5: Artifact Validation:** Implementation of `TestArtifactManager` for SHA-256 tamper-evident test datasets.
- [x] **Phase 9.6: Clock Safety HAL:** Introduced `IClockDiscipliner` abstraction with `NoOp` safety lock.

### Phase 10: Precision Drift & Offset Analysis (v0.8.0) - ACTIVE
**Objective:** Quantify clock accuracy using the Reactive Ingress data.
**Branch:** `feat/jitter-bug`
- [x] **Phase 10.1: The Reactive Offset Engine:** Calculate system drift ($T_{System} - T_{GPS}$) for every triggered pulse.
- [x] **Phase 10.2: Stream Stabilization (The Heartbeat):** Implementation of a non-blocking watchdog sentinel to replace the 5s poll.
- [x] **Phase 10.3: Signal Intelligence:** Extend the State Registry to handle GSV/GSA for SNR and Constellation tracking.
- [x] **Phase 10.4: Statistical Window:** Pure functional implementation of Jitter (RMS) and Mean Offset.

### Phase 11: Precision Clock Synchronization (v1.0.0)
**Objective:** Discipline the local host clock via a **Dual-Tier HAL**.
- [ ] **Phase 11.1: The PPS Bridge (Linux):** RFC 2783 support for <100µs kernel timestamping.
- [ ] **Phase 11.2: The Best-Effort Sync (Windows):** Coarse disciplining via Windows Time Service.
- [ ] **Phase 11.3: The Slew Engine:** Implementation of monotonic drift correction.
- [ ] **Phase 11.4: Privilege HAL:** Automated Root/Admin handling via feature flags.

### Phase 12: The QTH Hub Authority (v2.0.0)
**Objective:** Broadcast Stratum 1 authority to the shack network.
- [ ] **Phase 12.1: NTP Daemon:** Lightweight UDP Port 123 responder.
- [ ] **Phase 12.2: Multi-Source Arbitration:** Logic to choose the most accurate available authority.
- [ ] **Phase 12.3: Quality of Authority (QoA):** Metric calculating confidence based on jitter/stratum.
- [ ] **Phase 12.4: Shack Broadcast Discovery:** Implement mDNS/Zeroconf for automated hub identification.

### Phase 13: Dashboard - Embedded Web Scribe (v3.0.0)
**Objective:** Lightweight remote monitoring and visual regression testing.
- [ ] **Phase 13.1: Embedded Server:** Zero-dependency web server (e.g., Sun HttpServer).
- [ ] **Phase 13.2: Visual Quality Gate:** **Playwright** integration for UI regression and asset capture.

### Phase 14: Dashboard - RESTful Pulse API (v3.1.0)
**Objective:** Expose real-time Telemetry Pulses as JSON for 3rd party shack tools.
- [ ] **Phase 14.1: Pulse persistence:** Optional SQLite or CSV export for post-shack analysis.

### Phase 15: Dashboard - "The Visual Shack" (v4.0.0)
**Objective:** High-fidelity real-time telemetry rendering via p5.js.
- [ ] **Phase 14.1: Temporal Drift Plot:** Dual-trace offset history line graph.
- [ ] **Phase 14.2: Polar Sky Plot:** Celestial constellation map.
- [ ] **Phase 14.3: Jitter Stability Histogram:** Precision distribution curve.
- [ ] **Phase 14.4: SNR Signal Matrix:** Multi-GNSS signal strength bars.

### Phase 16: Heritage Stabilization (v5.0.0)
**Objective:** Final performance optimization and long-term security archival.

### Phase 17: Community & Outreach (The "Alpha Flight")
**Objective:** Transition `qtr-qth` from a private tool to a community-standard hub for Amateur Radio.
- [ ] **Phase 17.1: High-Fidelity Demonstration:** Use 'The Visual Shack' (Phase 15) to create video/gif demos for GitHub and QRZ.
- [ ] **Phase 17.2: Beta Flight Program:** Identify and onboard 5-10 Alpha Testers with diverse hardware (u-blox, MTK, SiRF).
- [ ] **Phase 17.3: Field Day Deployment:** Document a real-world mobile deployment for the ARRL Field Day or Winter Field Day.
- [ ] **Phase 17.4: APRS Integration Showcase:** Demonstrate real-time kinematic tracking via the VTG ingestion.
- [ ] **Phase 17.5: Publication:** Technical write-up for QST or CQ Magazine regarding precision shack timing.
- [ ] **Phase 17.6: Hardware Enclosures:** Research, design, and publish 3D-printable STL enclosures for target boards (e.g., 7SEMI NEO-M9N) to provide SMA strain relief and professional presentation.

---
## 📐 Platform Precision & Resolving Power (Epsilon)

| Platform | Timing Source | Resolving Power (Epsilon) | Precision Tier |
| :--- | :--- | :--- | :--- |
| **Linux (Kernel PPS)** | Serial DCD + RFC 2783 | **< 100 microseconds** | **Stratum 1 (Instrument)** |
| **Linux (NMEA Only)** | Serial Async | **50 - 150 milliseconds** | **Stratum 2 (Reference)** |
| **Windows 11** | User-Mode Async | **100 - 250 milliseconds** | **Coarse (Ref / Utility)** |
| **Simulation** | Frozen Clock | **Deterministic (0)** | **Verification** |

---
## 🧪 Current Quality Status
- **Test Coverage:** 92.7% | **Logic Integrity:** Certified High Fidelity.
- **Supply Chain:** Gradle Verification Enabled (SHA-256).
- **Architecture:** Pure Functional Assembly Line.
- **Portability:** NIO-based pathing (Win/Linux/RPi ready).

## ⚖️ Legal & Identity
- **Developer:** Nicholas R. Ustick ([N8QQQ](https://www.qrz.com/db/N8QQQ)) | **License:** GNU GPL v3.0
