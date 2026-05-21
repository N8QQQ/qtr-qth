---
layout: default
title: Manual Test Plan
---
# Manual Test Plan: qtr-qth End-to-End Verification

This document provides a structured protocol for manual verification of the `qtr-qth` system. It is designed to ensure "Heritage Grade" stability across different hardware and operating environments.

---

## 🛠️ Pre-requisites
- **Environment:** Java 21+ installed.
- **Hardware (Optional):** A USB GPS Receiver (u-blox, GlobalSat, etc.).
- **Tools:** Terminal (CMD, PowerShell, or Bash).

---

## 🧪 Scenario 1: The Pre-flight Check (The Doctor)
**Goal:** Verify that the system can correctly identify environment readiness.

1.  **Action:** Run the environment doctor.
    - Windows: `./qtr-qth.bat --doctor`
    - Linux/RPi: `./qtr-qth --doctor`
2.  **Expected Outcome:**
    - The output displays `[JAVA]`, `[SERIAL]`, `[NETWORK]`, and `[DOCKER]` checks.
    - If a dependency is missing, a `[RESCUE]` command is provided.
    - The audit completes with a "Success" summary.

---

## 🧪 Scenario 2: The Hardware Hunt (The Probe)
**Goal:** Verify that the system can auto-discover physical GPS hardware.

1.  **Action:** Connect your USB GPS device and run the probe.
    - Windows: `./qtr-qth.bat --probe`
    - Linux/RPi: `./qtr-qth --probe`
2.  **Expected Outcome:**
    - The tool lists all detected COM/TTY ports.
    - The "Discovery Analysis" identifies your GPS port as a "RECOMMENDED TARGET".
    - Port metadata (Description, Manufacturer) is displayed accurately.

---

## 🧪 Scenario 3: The Virtual Run (Simulation)
**Goal:** Verify system logic without physical hardware.

1.  **Action:** Ensure `simulation.mode=true` in `qtr-qth.properties` and start the app.
    - Windows: `./qtr-qth.bat`
    - Linux/RPi: `./qtr-qth`
2.  **Expected Outcome:**
    - The logs show `Mode: SIMULATION_LOCK`.
    - Telemetry pulses appear every second with a valid Pulse ID (e.g., `[B026]`).
    - Grid Square and NTP references are populated and updating.

---

## 🧪 Scenario 4: The Real Deal (Physical Hardware)
**Goal:** Verify end-to-end integration with a Stratum 0 source.

1.  **Action:** Set `simulation.mode=false` in `qtr-qth.properties`, connect your GPS, and start the app.
2.  **Expected Outcome:**
    - The logs show `Mode: HARDWARE_LOCK`.
    - The system identifies and opens the GPS port automatically.
    - Real UTC time and coordinates from the satellites appear in the console.

---

## 🧪 Scenario 5: The "Unplug" Stress Test (Recovery)
**Goal:** Verify the "Connection Neutralization" and self-healing logic.

1.  **Action:** While the application is running in `HARDWARE_LOCK` mode, **unplug the USB GPS receiver**.
2.  **Expected Outcome:**
    - The system detects the stream collapse.
    - Console logs: `SIGNAL LOSS DETECTED: Entering Adaptive Recovery...`
    - The status changes to `[GPS: RECOVERY | NTP: ACTIVE]`.
3.  **Action:** **Plug the GPS receiver back in.**
4.  **Expected Outcome:**
    - The system re-discovers the port during its next backoff cycle.
    - Confluence is restored, and telemetry pulses resume without a restart.

---

## 🧪 Scenario 6: The Log Audit
**Goal:** Verify observability standards.

1.  **Action:** Stop the application and navigate to the `logs/` directory.
2.  **Expected Outcome:**
    - `qtr-qth.log` exists and contains high-level operational events.
    - `trace.log` exists and contains granular second-by-second pulse data.
    - Open `trace.log` and verify that the Pulse IDs correlate across Raw and Final entries.

---
*Manual verification is the final gate for "Heritage Grade" certification.*
