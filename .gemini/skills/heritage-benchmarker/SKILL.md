---
name: heritage-benchmarker
description: Automates high-fidelity hardware benchmarks and registry updates for qtr-qth. Use this skill to execute cross-platform 115,200 baud live captures, extract precision metrics, and synchronize telemetry with BENCHMARK.md on both Windows and Linux hosts.
---

# Heritage Benchmarker

This skill orchestrates the extraction of ground-truth telemetry from live silicon.

## 🛰️ Workflow: Deterministic Capture

To execute an automated benchmark run (30s control):

1. **Environmental Lock**: Ensure physical GPS hardware is connected.
2. **Execution**: Execute the automation script using the absolute path provided in the skill resources.
   ```bash
   node <path-to-skill>/scripts/bench_record.cjs
   ```
3. **Registry Sync**: Copy the output metrics (Offset/Jitter) and update `benchmarks/BENCHMARK.md`.

## 🏆 Cross-Platform Logic

The skill automatically detects the host environment:
- **Windows (Athena)**: Archives to `benchmarks/windows-workstation/`.
- **Linux (Gandalf)**: Archives to `benchmarks/linux-laptop/`.
- **WSL2 (Mechanar)**: Archives to `benchmarks/linux-laptop/` (with virtualized context notes).

## 📊 Precision Standard

The script extracts the **FINAL** pulse from the stream to ensure the system has reached reactive lock. It converts the PT-format offset and microsecond jitter to standard milliseconds for the master registry.
