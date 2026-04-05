# Project Blueprint: qtr-qth

`qtr-qth` is a high-precision GPS Time (QTR) and Location (QTH) hub designed for mission-critical synchronization in Amateur Radio and technical shacks.

## 🏛️ Architectural Foundation
- **Hardware Abstraction Layer (HAL):** Decouples business logic from physical serial ports, enabling simulation and automated testing.
- **BDD/TDD Mandate:** All features are developed using Test-Driven Development and documented via Behavior-Driven Development fixtures.
- **LTS Stability:** Built on Java 21 LTS for long-term support and framework compatibility.
- **Observability:** Structured logging via SLF4J + Logback with dedicated "Shack" (Operation) and "Lab" (Trace) rolling files.

## 🏁 Definition of Done (DoD)
A task or phase is considered "Done" only when:
1.  **Functionality:** All requirements specified in the step are implemented and verified.
2.  **Test Coverage:** "Business Logic" (NMEA, Config, Timing) must maintain a minimum of **90% instruction coverage**.
3.  **Security & Integrity:** Data ingestion must include integrity checks (e.g., Checksums).
4.  **Operational Resilience:** Systems must handle abrupt shutdowns gracefully (Shutdown Hooks) and provide self-healing configurations.
5.  **Documentation:** Documentation must be bifurcated into user-focused (README.md) and technical-focused (DEVELOPER.md) guides.
6.  **Review:** Every feature must pass a final comprehensive code review (AI-assisted or peer).
7.  **Heritage Stability:** 
    - No business logic may be modified during a logging refactor.
    - Coverage percentage must remain stable or increase during refactoring.
    - Test failures during refactoring must be resolved without altering BDD semantics.

---

## 🗺️ Project Roadmap

### Phase 1: Infrastructure & Identity (COMPLETED)
- [x] Repository initialization and structure.
- [x] Project branding and standardizing package names (`com.stoicprogrammer.qtrqth`).
- [x] Baseline dependency management (Gradle).
- [x] **CI/CD Pipeline:** Automated GitHub Actions for builds, tests, and coverage.

### Phase 2: Serial Comms & NMEA Engine (COMPLETED - v0.1.0)
- [x] **Config Bootstrapping:** Support for `qtr-qth.properties` with smart defaults and self-healing.
- [x] **Port Discovery:** Automated hardware detection with configurable metadata keywords.
- [x] **NMEA Nibbler:** Asynchronous byte-stream ingestion into validated sentences.
- [x] **Parsing Engine:** Stateful extraction of UTC time, date, Lat/Lon, altitude, and satellite counts ($GPRMC, $GPGGA, $GPZDA).
- [x] **QTH Utility:** Maidenhead Grid Square calculation (6-character precision).
- [x] **GPS Simulator:** Virtual serial provider for lab-testing without hardware.
- [x] **Telemetry Diagnostics:** Configurable raw data logging toggle and XOR checksum validation.

### Phase 3: Observability Foundation (IN PROGRESS)
- [ ] **Structured Logging:** Implement SLF4J + Logback dependencies.
- [ ] **Dual-Log Strategy:** Implement split appenders for `qtr-qth.log` (INFO) and `trace.log` (DEBUG/TRACE).
- [ ] **Asynchronous Scribe:** Wrap all file appenders in `AsyncAppender` for non-blocking I/O.
- [ ] **Dynamic Logging:** Enable Logback auto-scanning for runtime level updates.
- [ ] **Surgical Refactor:** Replace `System.out` with semantic logging levels while preserving 99% coverage.
- [ ] **Traceability:** Implement MDC-based Trace IDs for NMEA pulse tracking.

### Phase 4: Network Time Reference (PLANNED)
- [ ] Implement robust NTP client for secondary time reference.
- [ ] Multi-server pooling and latency monitoring.

### Phase 5: Drift & Offset Analysis (PLANNED)
- [ ] Compare GPS precision time against NTP baseline.
- [ ] Calculate system clock drift and jitter scores.

### Phase 6: Precision Clock Synchronization (PLANNED)
- [ ] OS-specific implementation for updating system time (Windows/Linux).
- [ ] Precision scheduling (synchronizing on the top of the second).

### Phase 7: The "QTH Hub" (PLANNED)
- [ ] Implement UDP broadcast engine for live telemetry sharing.
- [ ] Support for JSON/XML output for 3rd party shack tools.

### Phase 8: Dashboard & Visualization (PLANNED)
- [ ] Real-time local dashboard (Swing or JavaFX).
- [ ] Satellite health and SNR visualization.

---
## 🧪 Current Quality Status
- **Current Baseline:** v0.1.0 (Phase 2 Deployed)
- **Business Logic Coverage:** 99.1%
- **System Integrity:** Verified via BDD Fixtures and System Integration Tests.

## ⚖️ Legal & Identity
- **Developer:** Nicholas R. Ustick (N8QQQ)
- **ORCID iD:** [0009-0001-9211-8000](https://orcid.org/0009-0001-9211-8000)
- **Copyright:** (c) 2026 Nicholas R. Ustick
- **License:** GNU General Public License v3.0 (Copyleft)
