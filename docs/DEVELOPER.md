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

---

## 🔌 Hardware Workspace Environments

Physical hardware telemetry (NMEA serial devices) must be routed to your local development workspace. Depending on your environment, follow the appropriate setup below:

### Option A: WSL2 Guest Workspace (Windows Host Setup)

When developing inside a WSL2 guest on a Windows host, physical USB serial/GPS hardware must be bridged. We use **usbipd-win** to forward the USB port.

#### Prerequisites (Windows Host)
1. Install the latest [usbipd-win MSI release](https://github.com/dorssel/usbipd-win/releases).
2. Install the WSL command-line tools in Linux:
   ```bash
   sudo apt install usbip-wsl
   ```

#### Hardware Passthrough Commands

1. **List Devices (Windows PowerShell/Command Prompt):**
   ```cmd
   usbipd list
   ```
   Identify your GPS device's BUSID (e.g., `9-4` for `COM8`).

2. **Bind Device (Windows Administrator command prompt - run once):**
   ```cmd
   usbipd bind --busid <BUSID>
   ```

3. **Attach to WSL (Windows regular command prompt):**
   ```cmd
   usbipd wsl attach --busid <BUSID>
   ```

4. **Verify inside WSL:**
   ```bash
   dmesg | grep tty
   ls /dev/ttyUSB*
   ```

---

### Option B: Native Linux Workspace Setup

On native Linux systems, USB serial/GPS hardware is automatically mounted under `/dev/ttyUSB*` or `/dev/ttyACM*`. However, regular users do not have permissions to read/write to serial TTY devices by default.

#### TTY Group Permissions Setup

1. **Run the local environment setup script:**
   This adds your user account to the correct dialout/serial permissions groups (e.g., `dialout`, `uucp`):
   ```bash
   chmod +x src/main/dist/scripts/linux-setup.sh
   ./src/main/dist/scripts/linux-setup.sh
   ```
2. **Apply Group Membership:**
   You must log out and log back in, or restart your system, for group permissions to take effect.
3. **Verify Access:**
   Confirm your user belongs to the target group and you can read the port:
   ```bash
   groups
   cat /dev/ttyUSB0
   ```

