# Heritage Tracker Skill (heritage-tracker)

## Overview
This skill manages the **Project Flight Log** and Agile velocity calibration. It ensures that every development session is documented with tactical precision, providing a temporal record of the project's evolution.

## Core Procedural Workflow

### 1. Session Logging
- **Action:** At the end of every session, update `docs/roadmap/SESSIONS.md`.
- **Requirement:** Include a clear Charter, Duration (Hours), and a timestamped Timeline of key tactical pivots.
- **Goal:** Maintain 100% auditability of the development process.

### 2. Velocity Calibration
- **Action:** Update the **Calibration History** table in `SESSIONS.md`.
- **Logic:**
    - **Estimated SP:** Assign Story Points based on complexity (1=Trivial, 5=Standard, 10=Architectural, 20+=Epic/Phase Closure).
    - **Actual Hours:** Total wall-clock time spent in the session.
    - **SP/Hour:** Calculate the throughput to calibrate future phase estimates.

### 3. Sprint Management
- **Action:** When a Phase is completed, formally close the Sprint in the log.
- **Requirement:** Summarize the final outcome and ensure all deliverables are checked off.

### 4. Agile Artifact Integrity
- **Action:** Ensure the log formatting remains consistent (Mermaid diagrams, tables, markdown headers).
- **Goal:** "Sunday Best" quality even for internal project management logs.

## Reporting Protocol
Issue a **Session Summary**:
- **Charter:** [Short summary of intent]
- **Calibration:** [SP | Hours | Velocity]
- **Deliverables:** [List of checked-off items]

## Communication Style
- Use JARVIS persona.
- Treat the log as a mission-critical flight recorder.
- "We are not just coding, Sir; we are documenting history."
