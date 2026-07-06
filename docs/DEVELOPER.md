# Developer Workflows: qtr-qth

This document outlines the local build pipeline, test suites, and containerized security scans for developers and architects working on the `qtr-qth` codebase.

---

## 🛠️ Building & Packaging

The project requires **JDK 21** or higher. It is built using Gradle. 

*   **Clean Build**:
    ```bash
    ./gradlew clean build
    ```
*   **Generate Dependency Verification Metadata**:
    If you upgrade dependencies or add library coordinates in `build.gradle.kts`, you must refresh the Gradle dependency verification hashes. Rerun the task without the cache:
    ```bash
    ./gradlew --write-verification-metadata sha256 --no-build-cache check --refresh-dependencies --rerun-tasks
    ```

---

## 🧪 Behavior-Driven Testing (BDD)

The test suite uses JUnit 5, AssertJ, and Mockito. All tests follow a strict **Given-When-Then** specification syntax matching the domain rules.

*   **Run All Tests**:
    ```bash
    ./gradlew test
    ```
*   **Coverage Reports**:
    The build generates a JaCoCo test coverage report. Run the tests and build the coverage report:
    ```bash
    ./gradlew test jacocoTestReport
    ```
    The report will be available at: `build/reports/jacoco/test/html/index.html`.

---

## 🛡️ Local Containerized CI (Quality Gates)

To prevent remote Actions runner costs, all style, syntax, vulnerability, and semantic security scans must be run locally in a Docker environment before merging to `main`.

*   **Run All Scans**:
    ```bash
    ./scripts/local-ci.sh --all
    ```
*   **Individual Quality Steps**:
    *   **Secret Detection (Gitleaks)**: `./scripts/local-ci.sh --secrets`
    *   **Vulnerability Scan (Trivy)**: `./scripts/local-ci.sh --vuln`
    *   **Style Linter (Super-Linter)**: `./scripts/local-ci.sh --lint`
    *   **Semantic Security (CodeQL)**: `./scripts/local-ci.sh --codeql`

*Note: The local Super-Linter image is heavy (~5GB) and requires Docker/Docker Compose to be active on adopting systems.*

---

## 📐 Architectural Purity (DAG Mandate)

This project strictly enforces a **Directed Acyclic Graph (DAG)** module dependency structure. Zero circular package references are tolerated. 

*   **Rule Enforcement**:
    Any package modifications must pass Checkstyle rules defined in `config/checkstyle/checkstyle.xml` and comply with the imports design outlined in the root [CONTEXT.md](file:///home/nicholas/src/qtr-qth/CONTEXT.md) glossary.
