# Project Blueprint: qtr-qth

`qtr-qth` is a high-precision GPS Time (QTR) and Location (QTH) hub designed for mission-critical synchronization in Amateur Radio and technical shacks.

## 🏛️ Architectural Foundation
- **Hardware Abstraction Layer (HAL):** Decouples business logic from physical serial ports, enabling simulation and automated testing.
- **BDD/TDD Mandate:** All features are developed using Test-Driven Development and documented via Behavior-Driven Development fixtures.
- **LTS Stability:** Built on Java 21 LTS for long-term support and framework compatibility.

## 🏁 Definition of Done (DoD)
A task or phase is considered "Done" only when:
1.  **Functionality:** All requirements specified in the step are implemented and verified.
2.  **Test Coverage:** "Business Logic" (NMEA, Config, Timing) must maintain a minimum of **90% instruction coverage**.
3.  **Security & Integrity:** Data ingestion must include integrity checks (e.g., Checksums). Code must be reviewed against OWASP Top 10 principles (Injection, Data Integrity, etc.).
4.  **Operational Resilience:** Systems must handle abrupt shutdowns gracefully (Shutdown Hooks) and provide self-healing configurations.
5.  **Documentation:** Documentation must be bifurcated into user-focused (README.md) and technical-focused (DEVELOPER.md) guides.
6.  **Standards:** BDD/TDD and HAL standards are strictly followed.
7.  **Review:** Every feature must pass a final comprehensive code review (AI-assisted or peer).

---

## 🗺️ Project Roadmap

### Phase 1: Infrastructure & Identity (COMPLETED)
- [x] Repository initialization and structure.
- [x] Project branding and standardizing package names (`com.stoicprogrammer.qtrqth`).
- [x] Baseline dependency management (Gradle).
- [x] **CI/CD Pipeline:** Automated GitHub Actions for builds, tests, and coverage.

### Phase 2: Serial Comms & NMEA Engine (COMPLETED)
- [x] **Config Bootstrapping:** Support for `qtr-qth.properties` with smart defaults.
- [x] **Port Discovery:** Automated hardware detection with configurable metadata keywords.
- [x] **NMEA Nibbler:** Asynchronous byte-stream ingestion into validated sentences.
- [x] **Parsing Engine:** Stateful extraction of UTC time, date, Lat/Lon, altitude, and satellite counts ($GPRMC, $GPGGA, $GPZDA).
- [x] **QTH Utility:** Maidenhead Grid Square calculation (6-character precision).
- [x] **GPS Simulator:** Virtual serial provider for lab-testing without hardware.
- [x] **Telemetry Diagnostics:** Configurable raw data logging toggle.

### Phase 3: Logging, Network & Timing Logic (IN PROGRESS)
- [ ] **Logging Infrastructure:** Implement commercial-grade structured logging (SLF4J + Logback) with rolling file support for 24/7 shack operation.
- [ ] **Tracing & Observability:** Integrate tracing for asynchronous NMEA ingestion and NTP sync events.
- [ ] Implement NTP client for secondary time reference.
- [ ] Develop "Drift Analysis" logic to compare GPS time vs. NTP time.
- [ ] Create Clock Synchronization interfaces.

### Phase 4: System Clock Synchronization (PLANNED)
- [ ] OS-specific implementation for updating system time (Windows/Linux).
- [ ] Precision scheduling (synchronizing on the top of the second).
- [ ] Stability monitoring (handling "leaps" and jitter).

### Phase 5: The "QTH Hub" (PLANNED)
- [ ] Implement UDP broadcast engine for live location sharing.
- [ ] Support for JSON or XML telemetry output for 3rd party shack tools.
- [ ] API for querying current Grid Square status.

### Phase 6: UI & Visual Dashboard (PLANNED)
- [ ] Development of a local dashboard (Swing or JavaFX).
- [ ] Real-time satellite health and SNR visualization.
- [ ] Map integration for current QTH visualization.

## ⚖️ Legal & Identity
- **Developer:** Nicholas R. Ustick (N8QQQ)
- **Organization:** StoicProgrammer.com
- **Assistant:** JARVIS (AI-Integrated Engineering)
- **Copyright:** (c) 2026 Nicholas R. Ustick
- **License:** GNU General Public License v3.0 (Copyleft)

---
## 🧪 Current Quality Status
- **Business Logic Coverage:** 99.1%
- **System Integrity:** Verified via BDD Fixtures and System Integration Tests.
