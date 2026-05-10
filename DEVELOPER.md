# Developer Documentation: qtr-qth

Welcome to the engine room. This document outlines the technical standards, build processes, and architecture for the `qtr-qth` project.

## 🏗️ Architectural Paradigm: The Functional Pipeline

`qtr-qth` has transitioned from a stateful OO approach to a **Pure Functional Pipeline**. Telemetry data flows through the system like an assembly line:

1.  **Conveyor Belt (`Stream`):** `SerialConnector` generates a continuous stream of verified NMEA sentences.
2.  **Contextual Wrapper (`TelemetryPulse`):** Each sentence is wrapped in a `TelemetryPulse` record which assigns a unique **Trace ID**.
3.  **Stateless Parser:** The `NmeaParser` is a pure function that transforms `(sentence, previousState)` into a `nextState`.
4.  **Pulse Tracking (MDC):** We utilize SLF4J's Mapped Diagnostic Context to tag every log entry with the Trace ID of the pulse that generated it.

## 🏗️ Development Workflow

### Running in Development
```powershell
./gradlew run
```

### Build Targets
- `installDist`: Creates an unzipped distribution in `build/install/qtr-qth/`.
- `test`: Executes the BDD suite with JVM flags for native access.

## 🤖 AI-Integrated Development

This project is developed using an **AI-first workflow** (Nicholas R. Ustick + JARVIS). All refactoring follows the **"Heritage Grade"** standard: Zero mutable logic in core components and strictly decoupled hardware abstractions.

## 🧪 Testing & Engineering Standards

### TDD & BDD
- **TDD:** No logic without a preceding failing test.
- **BDD Fixtures:** Tests are declared as behaviors (`given`, `when`, `then`).

### Code Coverage
The **Definition of Done (DoD)** requires **>90% instruction coverage** for "Business Logic."
- **Current Metric:** 93.5% total logic coverage.
- **Check Coverage:** `./gradlew test jacocoTestReport`

## 📡 Observability Architecture

- **Logging Facade:** SLF4J 2.0.
- **Logging Engine:** Logback 1.5.
- **Async Strategy:** All file appenders are wrapped in `AsyncAppender` to ensure that disk I/O latency never blocks the precision telemetry stream.

---
*For end-user instructions, see [README.md](README.md).*
