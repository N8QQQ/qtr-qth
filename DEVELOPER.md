# Developer Documentation: qtr-qth

Welcome to the engine room. This document outlines the technical standards, build processes, and architecture for the `qtr-qth` project.

## 🛠️ Technical Prerequisites

- **Java:** JDK 21 LTS (The project targets this version for framework stability).
- **Gradle:** 8.x (The included `./gradlew` wrapper is preferred).
- **Tooling:** JaCoCo (Coverage).

## 🏗️ Development Workflow

### Running in Development
Use the Gradle wrapper to build and execute the application with hot-reloading context:
```powershell
./gradlew run
```

### Build Targets
- `clean`: Wipes the `build` directory.
- `assemble`: Compiles code and packages standard JARs.
- `installDist`: Creates an unzipped distribution in `build/install/qtr-qth/`.

## 🤖 AI-Integrated Development

This project is developed using an **AI-first workflow**. 
- **Primary Architect:** Nicholas R. Ustick (N8QQQ)
- **Assistant:** JARVIS (via Gemini CLI)

All code generation and refactoring are performed in collaboration with an AI assistant that adheres to the project's strict TDD/BDD mandates and hardware abstraction standards.

## 🧪 Testing & Engineering Standards

We adhere to rigorous quality controls to ensure mission-critical reliability.

### TDD & BDD
- **TDD:** No functional logic is implemented without a preceding failing test.
- **BDD:** All tests follow the **Given-When-Then** Fixture pattern.
- **Base Class:** Inherit from `com.stoicprogrammer.qtrqth.base.BddTest` for standard assertion wrappers.

### Code Coverage
The **Definition of Done (DoD)** requires a minimum of **90% instruction coverage** for all "Business Logic" (NMEA, Config, Timing).
- **Exclusions:** Hardware-specific wrappers (`serial.jserialcomm.*`) and the `Main` class are excluded from percentage mandates.

Run coverage reports:
```powershell
./gradlew test jacocoTestReport
```
*Reports are located at: `build/reports/jacoco/test/html/index.html`*

## 📦 Distribution & Deliverables

To package the application for end-users:

1. **ZIP Bundle:** `./gradlew distZip` (Best for cross-platform compatibility).
2. **Local Install:** `./gradlew installDist` (Creates an unzipped version in `build/install/`).

## 🗺️ Project Vision & Roadmap

For a detailed breakdown of implemented features, architectural milestones, and the long-term vision for the project, refer to the **[Project Plan (PLAN.md)](PLAN.md)**.

---
*For end-user instructions, see [README.md](README.md).*
