# Developer Documentation: qtr-qth

This document outlines the technical environment and build workflows for the `qtr-qth` project. 

## 🏗️ Technical Architecture
For details on the functional pipeline, concurrency model, and system design, refer to the **[System Architecture (ARCHITECTURE.md)](ARCHITECTURE.md)**.

## 🛠️ Development Workflow

### Prerequisites
- **Java:** JDK 21 LTS (Temurin).
- **Gradle:** 9.3.0.

### Running in Development
```powershell
./gradlew run
```

### Build Targets
- `installDist`: Creates an unzipped distribution in `build/install/qtr-qth/`.
- `test`: Executes the BDD suite with JVM flags for native access.
- `jacocoTestReport`: Generates coverage metrics.

## 🤖 AI-Integrated Development
This project is developed using an **AI-first workflow** (Nicholas R. Ustick + JARVIS). All refactoring must follow the **"Heritage Grade"** standard: Zero mutable logic in core components and strictly decoupled hardware abstractions.

## 🧪 Engineering Standards

### TDD & BDD
- **TDD:** No logic without a preceding failing test.
- **BDD Fixtures:** Tests are declared as behaviors (`given`, `when`, `then`).

### Code Coverage
The **Definition of Done (DoD)** requires **>90% instruction coverage** for "Business Logic."
- **Check Coverage:** `./gradlew test jacocoTestReport`

---
*For end-user instructions, see [README.md](README.md).*
