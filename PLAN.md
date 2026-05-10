# Project Blueprint: qtr-qth

`qtr-qth` is a high-precision GPS Time (QTR) and Location (QTH) hub designed for mission-critical synchronization in Amateur Radio and technical shacks.

## 🏛️ Architectural Foundation
- **Hardware Abstraction Layer (HAL):** Decouples business logic from physical serial ports.
- **Functional Pipeline:** Telemetry is processed as an immutable stream of state transformations.
- **Observability:** Structured logging via SLF4J + Logback with Async rolling files and MDC-based Trace IDs.
- **LTS Stability:** Java 21 LTS baseline.

## 🏁 Definition of Done (DoD)
A task or phase is considered "Done" only when:
1.  **Functionality:** Requirements implemented and verified.
2.  **Test Coverage:** "Business Logic" must maintain **>90% instruction coverage**.
3.  **Heritage Stability:** No business logic modified during logging/observability refactors.
4.  **Operational Resilience:** Non-blocking async I/O for all background tasks.

---

## 🗺️ Project Roadmap

### Phase 1: Infrastructure & Identity (COMPLETED)
- [x] Repository initialization and standardizing package names.
- [x] **CI/CD Pipeline:** Automated GitHub Actions and JaCoCo artifacts.

### Phase 2: Serial Comms & NMEA Engine (COMPLETED - v0.1.0)
- [x] **NMEA Nibbler:** Asynchronous byte-stream ingestion.
- [x] **Parsing Engine:** Pure functional extraction of UTC time, date, Lat/Lon, and altitude.
- [x] **GPS Simulator:** Virtual serial provider for lab-testing.
- [x] **QTH Utility:** Maidenhead Grid Square calculation.

### Phase 3: Observability Foundation (COMPLETED - v0.2.0 Candidate)
- [x] **Structured Logging:** SLF4J + Logback integration.
- [x] **Dual-Log Strategy:** Split Shack (Info) and Lab (Debug) files.
- [x] **Asynchronous Scribe:** Non-blocking file I/O using `AsyncAppender`.
- [x] **Traceability:** MDC-based Trace IDs (Pulse IDs) for end-to-end telemetry tracking.
- [x] **Dynamic Control:** XML auto-scanning for runtime level updates.

### Phase 4: Network Time Reference (PLANNED)
- [ ] Implement robust NTP client for secondary time reference.
- [ ] Multi-server pooling and latency monitoring.

### Phase 5: Drift & Offset Analysis (PLANNED)
- [ ] Compare GPS precision time against NTP baseline.
- [ ] Calculate system clock drift and jitter scores.

### Phase 6+: Synchronization & Dashboard (PLANNED)
- [ ] OS-specific clock updates (Windows/Linux).
- [ ] Real-time local dashboard (Swing or JavaFX).

---
## 🧪 Current Quality Status
- **Current Baseline:** Phase 3 Certified
- **Instruction Coverage:** 93.5%
- **System Integrity:** Verified via MDC-tracked bench tests.

## ⚖️ Legal & Identity
- **Developer:** Nicholas R. Ustick (N8QQQ) | **License:** GNU GPL v3.0
