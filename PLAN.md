# Project Blueprint: qtr-qth

`qtr-qth` is a high-precision GPS Time (QTR) and Location (QTH) hub designed for mission-critical synchronization in Amateur Radio and technical shacks.

## 🏛️ Technical Design
For details on the concurrency model and functional design patterns, refer to the **[System Architecture (ARCHITECTURE.md)](ARCHITECTURE.md)**.

## 🔄 Agile Governance: Session-Based Agile (SBA)
The project follows a hybrid **SBA** methodology to maintain "Heritage Grade" quality in a hobbyist environment:
- **Sprints:** 2-week strategic milestones.
- **Sessions:** Tactical development blocks (typically 2-4 hours).
- **Atomic Phases:** Roadmap phases are partitioned into units of **2-3 Story Points**, targeting completion of 1-2 units per session.
- **Calibration:** We track **Story Points per Session Hour** ($SP / h$) to calibrate future capacity and project completion dates.
- **Business Value (BV):** Priority assigned on a scale of **100 to 1000** (1000 = Critical Mission Impact).

## 🏁 Definition of Done (DoD)
A task or phase is considered "Done" only when:
1.  **Functionality:** Requirements implemented and verified via BDD suites.
2.  **Test Coverage:** "Business Logic" must maintain **>90% instruction coverage**.
3.  **Security (OWASP 2025):** Code hardened against [OWASP Top 10 (2025)](https://owasp.org/Top10/2025/) standards.
4.  **Static Analysis:** Mandatory review for security concerns (Injection, Exceptional Conditions) and logic flaws.
5.  **Heritage Stability:** No business logic modified during logging/observability refactors.
6.  **Operational Resilience:** Non-blocking async I/O and graceful shutdown hooks must be preserved.

---

## 🗺️ Project Roadmap

### Phase 1 & 2: Foundations & Hardware (COMPLETED - v0.1.0)
- [x] Repository structure, Gradle setup, and CI/CD.
- [x] **NMEA Engine:** Asynchronous ingestion and functional parsing ($GPRMC, $GPGGA, $GPZDA).
- [x] **GPS Simulator:** decoupling hardware via HAL.
- [x] **QTH Utility:** Maidenhead Grid Square calculation.

### Phase 3: Observability Foundation (COMPLETED - v0.2.0)
- [x] **Logging:** SLF4J + Logback with dual Async rolling files (Shack/Lab).
- [x] **Traceability:** MDC-based Trace IDs for end-to-end pulse tracking.
- [x] **Hardening:** OWASP 2025 compliant input sanitization and exception management.

### Phase 4: Network Time Reference (COMPLETED - v0.3.0)
- [x] **Phase 4.1: Supply Chain & Console UX**
- [x] **Phase 4.2: Baseline NTP Client**
- [x] **Phase 4.3: Precision Metadata**
- [x] **Phase 4.4: Pipeline Integration**
- [x] **Phase 4.5: Resilience & Multi-Pooling**

### Phase 5: Drift & Offset Analysis (v0.4.0)
**Objective:** Quantify the accuracy of the system clock.
**Branch:** `feat/jitter-bug`
- **Phase 5.1: The Offset Engine (The "Math")** (SP: 3 | BV: 900)
  - [ ] **5.1.1: System Clock Sampler:** Capture `System.nanoTime()` at pulse arrival.
  - [ ] **5.1.2: Differential Calculator:** Compare System Time vs. GPS (QTR) vs. NTP.
  - [ ] **5.1.3: Reference Prioritization:** Logic to weight GPS vs. NTP based on metadata.
- **Phase 5.2: Jitter & Statistics (The "Stability")** (SP: 2 | BV: 400)
  - [ ] **5.2.1: Sliding Window Buffer:** Memory-efficient storage for last 60 samples.
  - [ ] **5.2.2: Standard Deviation & Mean:** Calculate average offset and jitter.
  - [ ] **5.2.3: Stability Rating Engine:** Heuristic "Shack-Grade" (S-Grade, A-Grade, etc.).
- **Phase 5.3: Observability Update (The "Report")** (SP: 1 | BV: 300)
  - [ ] **5.3.1: Drift Logging:** Enhance pulse logs with drift and jitter metadata.
  - [ ] **5.3.2: Health Pulse:** Periodic summary log entry detailing overall time health.

### Phase 6: Precision Clock Synchronization (v1.0.0)
**Objective:** Update the system clock with precision and authority.
**Branch:** `feat/chrono-trigger`
- [ ] **Phase 6.1: OS Permission HAL** (SP: 2 | BV: 700)
- [ ] **Phase 6.2: Windows Time Service Provider** (SP: 3 | BV: 600)
- [ ] **Phase 6.3: Linux/Pi Time Service Provider** (SP: 3 | BV: 600)
- [ ] **Phase 6.4: Precision Sync Scheduling** (SP: 3 | BV: 800)

### Phase 7: The "QTH Hub" (v2.0.0)
**Objective:** Broadcast live telemetry to 3rd party shack tools.
**Branch:** `feat/stratum-shack`
- [ ] **Phase 7.1: UDP Broadcast Engine** (SP: 3 | BV: 500)
- [ ] **Phase 7.2: Serialization Logic (JSON/NMEA)** (SP: 2 | BV: 400)
- [ ] **Phase 7.3: Broadcast Pipeline Integration** (SP: 2 | BV: 400)

### Phase 8: Dashboard - Embedded Web Scribe (v3.0.0)
**Objective:** Lightweight embedded server for raw telemetry capture.
- [ ] **Phase 8.1: Embedded Jetty/Spark Orchestration** (SP: 3 | BV: 300)
- [ ] **Phase 8.2: Secure Context Handling** (SP: 2 | BV: 300)

### Phase 9: Dashboard - RESTful Pulse API (v3.1.0)
**Objective:** Expose real-time telemetry via structured endpoints.
- [ ] **Phase 9.1: Live Pulse Endpoint** (SP: 2 | BV: 400)
- [ ] **Phase 9.2: Historical Drift Snapshot API** (SP: 2 | BV: 300)

### Phase 10: Dashboard - "Shack-View" HTML (v4.0.0)
**Objective:** High-fidelity, real-time visual monitoring dashboard.
- [ ] **Phase 10.1: Reactive UI Components (Clock/Map)** (SP: 3 | BV: 500)
- [ ] **Phase 10.2: WebSocket Pulse Streaming** (SP: 2 | BV: 400)

### Phase 11: Heritage Stabilization (v5.0.0)
**Objective:** Final hardening, documentation audit, and performance tuning.
- [ ] **Phase 11.1: Long-haul Stress Testing** (SP: 3 | BV: 600)
- [ ] **Phase 11.2: SDK/Library Dependency Audit** (SP: 2 | BV: 400)
- [ ] **Phase 11.3: "Final Seal" Documentation Review** (SP: 1 | BV: 500)

---
## 🧪 Current Quality Status
- **Baseline Velocity:** 5.45 SP/Hour (High-fidelity SBA calibration).
- **Projected Work Remaining:** 32 Story Points.
- **Estimated Completion Effort:** ~6 Session Hours.
- **Instruction Coverage:** 92.7% | **Security:** OWASP 2025 compliant.
- **Trailing Action (SOW-05):** Update feature branching policy to include `CITATION.cff` version bumps.

## ⚖️ Legal & Identity
- **Developer:** Nicholas R. Ustick (N8QQQ) | **License:** GNU GPL v3.0
