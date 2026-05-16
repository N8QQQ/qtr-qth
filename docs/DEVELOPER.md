# Developer Documentation: qtr-qth

This document outlines the technical environment, build workflows, and formal release procedures for the `qtr-qth` project. 

## 🏗️ Technical Architecture
For details on the functional pipeline, concurrency model, and system design, refer to the **[System Architecture (ARCHITECTURE.md)](ARCHITECTURE.md)**.

## 🛠️ Development Workflow

### Prerequisites
- **Java:** JDK 21 LTS (Temurin).
- **Gradle:** 9.3.0.

### Build & Run
- `./gradlew run`: Start the application.
- `./gradlew installDist`: Create unzipped distribution in `build/install/qtr-qth/`.
- `./gradlew test`: Execute the BDD suite.

## 🤖 AI-Integrated Development
This project follows an **AI-first workflow** (Nicholas R. Ustick + JARVIS). All refactoring must follow the **"Heritage Grade"** standard: Zero mutable logic in core components and strictly decoupled hardware abstractions. 

**Note:** The strict functional programming constraints (Streams, Immutability, Optional) and the Senior Java Architect persona are formalized in [`GEMINI.md`](GEMINI.md). Both AI agents and human contributors are expected to adhere to these mandates.


## 🧪 Engineering Standards
- **TDD/BDD:** No logic without a preceding failing test. Tests use Behaviors (`given`, `when`, `then`).
- **Code Coverage:** "Business Logic" must maintain **>90% instruction coverage**.

## 🚀 Heritage Release Protocol

To ensure archival integrity and high-fidelity handovers, the following protocol is **mandatory** for every release:

### 1. Pre-Release Metadata Sync
- [ ] Bump version in `build.gradle.kts`.
- [ ] Update `version` and `date-released` in `CITATION.cff`.
- [ ] Ensure all **Atomic Phases** in `PLAN.md` are marked complete.

### 2. Logic & Security Certification (DoD)
- [ ] **Test Pass:** Run `./gradlew clean test`. **All tests must be green.**
- [ ] **Coverage Check:** Verify `./gradlew jacocoTestReport` meets the **>90%** mandate.
- [ ] **OWASP 2025 Audit:** Manual/Static scan for Injection, Supply Chain, and Integrity vulnerabilities.
- [ ] **Complexity Review:** Identify and refactor "Cognitive Hotspots" to maintain simple, functional logic.

### 3. Distribution Hardening
- [ ] **Artifact Generation:** Run `./gradlew distZip`.
- [ ] **Verification:** Confirm `build/distributions/qtr-qth-[version].zip` is present and functional.

### 4. Collaborative Review (The Nick-Gate)
- [ ] **Branch Push:** Push all changes to the feature branch.
- [ ] **Pull Request:** Create a formal PR against `main`.
- [ ] **Automation:** Allow GitHub Actions to complete all CI/CD checks.
- [ ] **Manual Inspection:** Hand over the PR for Nick's manual review.

### 5. Final Archival
- [ ] **Merge:** Squash and merge into `main` after approval.
- [ ] **Tag & Release:** Create a GitHub Release and **attach the ZIP distribution asset**.

---
*For end-user instructions, see [README.md](README.md).*.
