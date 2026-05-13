# Project Blueprint: qtr-qth

`qtr-qth` is a high-precision GPS Time (QTR) and Location (QTH) hub designed for mission-critical synchronization in Amateur Radio and technical shacks.

## 🏛️ Architectural Foundation
- **Hardware Abstraction Layer (HAL):** Decouples business logic from physical serial ports.
- **Functional Pipeline:** Telemetry is processed as an immutable stream of state transformations.
- **Observability:** Structured logging via SLF4J + Logback with Async rolling files and MDC-based Trace IDs.
- **LTS Stability:** Built on Java 21 LTS baseline.

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

### Phase 4: Network Time Reference (v0.3.0)
**Objective:** Establish a professional network-based time reference for drift comparison.
**Branch:** `feat/ntp-nibbles`
- **Phase 4.1: Supply Chain & Console UX** (COMPLETED)
  - [x] **SOW-01:** Implement Gradle Dependency Verification (SHA-256 signatures).
  - [x] **UX-01:** Simplify console log pattern to focus on raw GPS/Time telemetry for end-users.
- **Phase 4.2: Baseline NTP Client** (SP: 2 | BV: 800)
  - [ ] Implement robust NTP poll to `pool.ntp.org` returning raw epoch time.
- **Phase 4.3: Precision Metadata** (SP: 3 | BV: 500)
  - [ ] Capture RTT (Round-trip time), Stratum, and Root Dispersion.
- **Phase 4.4: Pipeline Integration** (SP: 2 | BV: 500)
  - [ ] Integrate NTP telemetry into the functional pipeline with MDC Trace IDs.
- **Phase 4.5: Resilience & Multi-Pooling** (SP: 3 | BV: 300)
  - [ ] Implement fallback pool manager for unreachable NTP hosts.
  - [ ] Handle network timeouts and Jitter threshold filtering.

### Phase 5: Drift & Offset Analysis (v0.4.0)
**Objective:** Quantify the accuracy of the system clock.
**Branch:** `feat/jitter-bug`
- **Phase 5.1: Clock Differential Logic** (SP: 3 | BV: 900)
- **Phase 5.2: Jitter & Stability Scoring** (SP: 2 | BV: 400)

### Phase 6: Precision Clock Synchronization (v1.0.0)
**Objective:** Update the system clock with precision and authority.
**Branch:** `feat/chrono-trigger`
- **Phase 6.1: OS Permission HAL** (SP: 2 | BV: 700)
- **Phase 6.2: Windows Time Service Provider** (SP: 3 | BV: 600)
- **Phase 6.3: Linux/Pi Time Service Provider** (SP: 3 | BV: 600)
- **Phase 6.4: Precision Sync Scheduling** (SP: 3 | BV: 800)

### Phase 7: The "QTH Hub" (v2.0.0)
**Objective:** Broadcast live telemetry to 3rd party shack tools.
**Branch:** `feat/stratum-shack`
- **Phase 7.1: UDP Broadcast Engine** (SP: 3 | BV: 500)
- **Phase 7.2: Serialization Logic (JSON/NMEA)** (SP: 2 | BV: 400)
- **Phase 7.3: Broadcast Pipeline Integration** (SP: 2 | BV: 400)

### Phase 8: Dashboard & Visualization (v3.0.0+)
**Objective:** Real-time visual monitoring of shack time health.
**Branch:** `feat/telemetry-tapestry`
- **Phase 8.1: Embedded Web Scribe** (SP: 3 | BV: 300)
- **Phase 8.2: RESTful Pulse API** (SP: 2 | BV: 300)
- **Phase 8.3: "Shack-View" HTML Dashboard** (SP: 3 | BV: 400)

---
## 🧪 Current Quality Status
- **Baseline Velocity:** 1.25 SP/Hour
- **Projected Work Remaining:** 38 Story Points.
- **Estimated Completion Effort:** ~30 Session Hours.
- **Instruction Coverage:** 92.6% | **Security:** OWASP 2025 compliant.

## ⚖️ Legal & Identity
- **Developer:** Nicholas R. Ustick (N8QQQ) | **License:** GNU GPL v3.0
