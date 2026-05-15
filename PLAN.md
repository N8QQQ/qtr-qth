# Project Blueprint: qtr-qth

`qtr-qth` is a high-precision GPS Time (QTR) and Location (QTH) hub designed for mission-critical synchronization in Amateur Radio and technical shacks.

## 🏛️ Technical Design
For details on the concurrency model, functional pipeline, and architectural decisions, see [ARCHITECTURE.md](ARCHITECTURE.md).

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

### Phase 5: The Functional Blueprint (v0.4.0)
**Objective:** Codify branchless logic and automated immutability mandates.
**Branch:** `feat/monad-weaver`
- [x] **Phase 5.1: Functional Purity Refactor:** Elimination of `if/else` and `switch` in core logic.
- [x] **Phase 5.2: Immutability Enforcement:** Mandatory `final` keywords and `record` usage.
- [x] **Phase 5.3: Automated Linter Integration:** Checkstyle 'Bulletproof' ruleset.
- [x] **Phase 5.4: Test Suite Reconciliation:** Refactored tests for linter compliance and functional retries.

### Phase 6: Drift & Offset Analysis (v0.5.0)
**Objective:** Quantify the accuracy of the system clock.
**Branch:** `feat/jitter-bug`
- [ ] **Phase 6.1: The Offset Engine (The "Math")** (SP: 3 | BV: 900)
  - [ ] **6.1.1: System Clock Sampler:** Capture `System.nanoTime()` at pulse arrival.
  - [ ] **6.1.2: Differential Calculator:** Compare System Time vs. GPS (QTR) vs. NTP.
  - [ ] **6.1.3: Reference Prioritization:** Logic to weight GPS vs. NTP based on metadata.
- [ ] **Phase 6.2: Jitter & Statistics (The "Stability")** (SP: 2 | BV: 400)
  - [ ] **6.2.1: Sliding Window Buffer:** Memory-efficient storage for last 60 samples.
  - [ ] **6.2.2: Standard Deviation & Mean:** Calculate average offset and jitter.
  - [ ] **6.2.3: Stability Rating Engine:** Heuristic "Shack-Grade" (S-Grade, A-Grade, etc.).
- [ ] **Phase 6.3: Observability Update (The "Report")** (SP: 1 | BV: 300)
  - [ ] **6.3.1: Drift Logging:** Enhance pulse logs with drift and jitter metadata.
  - [ ] **6.3.2: Health Pulse:** Periodic summary log entry detailing overall time health.

### Phase 7: Precision Clock Synchronization (v1.0.0)
**Objective:** Update the system clock with precision and authority.
**Branch:** `feat/chrono-trigger`
- [ ] **Phase 7.1: OS Permission HAL** (SP: 2 | BV: 700)
- [ ] **Phase 7.2: Windows Time Service Provider** (SP: 3 | BV: 600)
- [ ] **Phase 7.3: Linux/Pi Time Service Provider** (SP: 3 | BV: 600)
- [ ] **Phase 7.4: Precision Sync Scheduling** (SP: 3 | BV: 800)

### Phase 8: The "QTH Hub" (v2.0.0)
**Objective:** Broadcast live telemetry to 3rd party shack tools.
**Branch:** `feat/stratum-shack`
- [ ] **Phase 8.1: UDP Broadcast Engine** (SP: 3 | BV: 500)
- [ ] **Phase 8.2: Serialization Logic (JSON/NMEA)** (SP: 2 | BV: 400)
- [ ] **Phase 8.3: Broadcast Pipeline Integration** (SP: 2 | BV: 400)

### Phase 9: Dashboard - Embedded Web Scribe (v3.0.0)
**Objective:** Lightweight embedded server for raw telemetry capture.
- [ ] **Phase 9.1: Embedded Jetty/Spark Orchestration** (SP: 3 | BV: 300)
- [ ] **Phase 9.2: Secure Context Handling** (SP: 2 | BV: 300)

### Phase 10: Dashboard - RESTful Pulse API (v3.1.0)
**Objective:** Expose real-time telemetry via structured endpoints.
- [ ] **Phase 10.1: Live Pulse Endpoint** (SP: 2 | BV: 400)
- [ ] **Phase 10.2: Historical Drift Snapshot API** (SP: 2 | BV: 300)

### Phase 11: Dashboard - "Shack-View" HTML (v4.0.0)
**Objective:** High-fidelity, real-time visual monitoring dashboard.
- [ ] **Phase 11.1: Reactive UI Components (Clock/Map)** (SP: 3 | BV: 500)
- [ ] **Phase 11.2: WebSocket Pulse Streaming** (SP: 2 | BV: 400)

### Phase 12: Heritage Stabilization (v5.0.0)
**Objective:** Final hardening, documentation audit, and performance tuning.
- [ ] **Phase 12.1: Long-haul Stress Testing** (SP: 3 | BV: 600)
- [ ] **Phase 12.2: SDK/Library Dependency Audit** (SP: 2 | BV: 400)
- [ ] **Phase 12.3: "Final Seal" Documentation Review** (SP: 1 | BV: 500)

---
## 🧪 Current Quality Status
- **Test Coverage:** 92.7% | **Logic Integrity:** Certified High Fidelity.
- **Supply Chain:** Gradle Verification Enabled (SHA-256).
- **Architecture:** Pure Functional Assembly Line (Two-River Confluence).
- **Documentation:** OWASP 2025 compliant.
- **Trailing Action (SOW-05):** Update feature branching policy to include `CITATION.cff` version bumps.

## ⚖️ Legal & Identity
- **Developer:** Nicholas R. Ustick (N8QQQ) | **License:** GNU GPL v3.0
