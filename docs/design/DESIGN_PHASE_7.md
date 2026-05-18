# Phase 7 Design: The Virtual Shack (v0.5.0)

**Objective:** Transition to a platform-agnostic development environment using Docker Compose, mirroring physical hardware and ARM64 architecture (Raspberry Pi) with high fidelity.

---

## 🏛️ Architectural Goals
1.  **Universal Orchestration:** Replace all `.ps1` and `.sh` scripts with `docker-compose.yml` to ensure identical developer workflows across Windows, Linux, and Mac.
2.  **The Mini-Lab (Phantom Shack):** A dedicated container providing a full development environment (JDK, Gradle, Serial Tools) with both virtual (socat) and physical hardware access.
3.  **Hardware Spoofing:** Utilize `socat` to create a virtual serial device (`/dev/ttyUSB99`) within the container for deterministic telemetry testing.
4.  **Architecture Parity:** Implement QEMU-based ARM64 emulation to certify Pi-compatibility locally.
5.  **Clean Repository Standards:** Move all infrastructure assets into an isolated `/docker` hierarchy.

---

## 🛠️ Tactical Components

### 7.1: Universal Command Center (`docker-compose.yml`)
- **Service: `ci`** - The Quality Gate (Testing, Linting, Artifacts).
- **Service: `docs`** - The Staging Hub (Jekyll Hub with p5.js flare).
- **Service: `phantom`** - The Virtual Shack (Interactive Dev Mirror + Hardware).
    - **Base OS:** Debian-slim (Raspbian Parity).
    - **Resource Hardening:** Enforce memory limits (e.g., 1GB) and CPU capping to simulate Raspberry Pi 4/5 hardware envelopes.

### 7.2: The Hardware Discovery Probe
- **Utility:** A standalone CLI tool built into the `serial` package.
- **Function:** Enumerate USB/Serial devices and apply fuzzy-matching logic to auto-identify physical GPS receivers.

### 7.3: Multi-Arch Build Pipeline
- **Parity:** Ensure `Dockerfile.ci` and `Dockerfile.phantom` can build for both `amd64` and `arm64`.
- **Stress Testing:** Implement an optional `stress-test` profile in Compose to execute the full gate under ARM64 emulation.

---

## 📂 Repository Reorganization
```text
/docker
  ├── ci/           # Quality Gate Assets
  ├── docs/         # Staging Hub Assets
  └── phantom/      # Mini-Lab Assets & Entrypoints
```

---

## 📈 Quality Gate Requirements
- [x] **Script Independence:** No reliance on host-level PowerShell or Bash for core workflows.
- [ ] **ARM64 Certification:** Application certified via emulated ARM64 container.
- [ ] **Hardware Pass-through:** Container successfully reads data from a physical USB GPS device (Linux/Pi native).
- [ ] **Spoof Integrity:** `NmeaParser` processes data from a virtual TTY pipe with zero-jitter.

---
*Maintained by JARVIS for the Heritage Grade Standard.*
