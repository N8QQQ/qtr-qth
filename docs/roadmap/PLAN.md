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

### Phase 7: The Virtual Shack (v0.5.0) - IN PROGRESS
**Objective:** Cross-platform simulation of Linux/ARM64 and physical hardware.
**Branch:** `feat/phantom-shack-logic`
- [x] **Phase 7.1: Universal Orchestration:** Implemented `docker-compose.yml` for platform-agnostic development.
- [x] **Phase 7.2: Hardware Spoofing (socat):** Certified virtual TTY (`/dev/ttyUSB99`) with NMEA background streaming.
- [ ] **Phase 7.3: Graceful Hardware Fallback:** Refactor `SystemOrchestrator` to prioritize hardware mode; implement auto-failover to `SimulationSerialProvider` if discovery fails.
- [x] **Phase 7.4: Multi-Arch & Resource Hardening:** Certified ARM64 Pi-parity and enforced hardware-accurate resource constraints.
- [x] **Phase 7.5: Live GPS Coordinator:** Implemented TCP bridge (Port 9999) for host-to-container hardware streaming.
- [ ] **Phase 7.6: Infrastructure Decommissioning:** Implement automated cleanup protocols and stateless container lifecycles.

### Phase 8: Temporal Virtualization (v0.6.0)
**Objective:** Decouple time-awareness for deterministic math verification.
**Branch:** `feat/chrono-tesseract`
- [ ] **Phase 8.1: Clock Dependency Injection:** Eliminate `Instant.now()`.
- [ ] **Phase 8.2: Jitter Testing Baseline:** Verify math using `Clock.fixed()`.

### Phase 9: Drift & Offset Analysis (v0.7.0)
**Objective:** Quantify the accuracy of the system clock.
**Branch:** `feat/jitter-bug`
- [ ] **Phase 9.1: The Offset Engine (The "Math")**
- [ ] **Phase 9.2: Jitter & Statistics (The "Stability")**
- [ ] **Phase 9.3: Observability Update (The "Report")**

### Phase 10: Precision Clock Synchronization (v1.0.0)
**Objective:** Update the system clock with precision and authority.
- [ ] **Phase 10.1: OS Permission HAL**
- [ ] **Phase 10.2: Windows Time Service Provider**
- [ ] **Phase 10.3: Linux/Pi Time Service Provider**

### Phase 11: The "QTH Hub" (v2.0.0)
**Objective:** Broadcast live telemetry to 3rd party shack tools.

### Phase 12: Dashboard - Embedded Web Scribe (v3.0.0)
### Phase 13: Dashboard - RESTful Pulse API (v3.1.0)
### Phase 14: Dashboard - "Shack-View" HTML (v4.0.0)
### Phase 15: Heritage Stabilization (v5.0.0)

---
## 🧪 Current Quality Status
- **Test Coverage:** 92.7% | **Logic Integrity:** Certified High Fidelity.
- **Supply Chain:** Gradle Verification Enabled (SHA-256).
- **Architecture:** Pure Functional Assembly Line.
- **Portability:** NIO-based pathing (Win/Linux/RPi ready).

## ⚖️ Legal & Identity
- **Developer:** Nicholas R. Ustick ([N8QQQ](https://www.qrz.com/db/N8QQQ)) | **License:** GNU GPL v3.0
