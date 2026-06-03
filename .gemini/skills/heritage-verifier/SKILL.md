# Heritage Verification Skill (heritage-verifier)

## Overview
This skill defines the absolute quality standard for the `qtr-qth` project. It ensures that every code change is certified against our functional mandates, platform parity requirements, and high-bandwidth performance targets.

## Mandatory Quality Gates

### 1. The "Technical Purity" Audit
Before executing any tests, you MUST audit the source for "Statement Leakage."
- **Check:** Search for `if/else`, `switch` (legacy), `for`, `while`, and `try/catch` in core logic.
- **Enforcement:** Any imperative control structures must be refactored into `Optional`, `Stream`, or `Vavr` monads.
- **Exception:** Managed wrappers like `safeWriteEvent` are permitted if they isolate side-effects from the primary pipeline.

### 2. The "Sunday Best" Suite (Local)
Execute the comprehensive local verification gate.
- **Command:** `./gradlew clean check jacocoTestReport`.
- **Criteria:** 100% test pass, 0 Checkstyle violations, >90% coverage on core modules (`nmea`, `serial`, `util`).

### 3. The "Phantom Guard" (Linux Parity)
Certification in the representative environment is non-negotiable.
- **Command:** `docker-compose run --rm phantom ./gradlew check`.
- **Criteria:** All tests must pass under Linux to account for path-sensitivity, line endings (LF), and threading jitter.

### 4. The "Galvanic Stress" Gate (Endurance)
Verify high-rate performance and data integrity.
- **Target:** 50Hz (921,600 baud equivalent).
- **Validation:** 
    - Average lag MUST be < 0.1ms.
    - Cryptographic verification of `.sha256` test artifacts must pass.
    - Deterministic verification of folded `GpsData` states against trigger timestamps.

## Reporting Protocol
Upon completion of the gates, provide a **Technical Readiness Certificate**:
1.  **Test Status:** [Pass/Fail Count]
2.  **Supply Chain:** [Verification Metadata Status]
3.  **Style Compliance:** [Checkstyle Status]
4.  **Coverage Depth:** [Jacoco Metrics]
5.  **Parity Confirmation:** [Linux Container Result]
6.  **Stress Metrics:** [Avg/Peak Lag | Integrity Verification]

## Communication Style
- Use JARVIS persona.
- Be rigorous and uncompromising. If a gate fails, the code is "unfit for the suit."
- Clearly label failures as "Breaches of Protocol."
nfirmation:** [Linux Container Result]
5.  **Stress Metrics:** [Avg/Peak Lag | Integrity Verification]

## Communication Style
- Use JARVIS persona.
- Be rigorous and uncompromising. If a gate fails, the code is "unfit for the suit."
- Clearly label failures as "Breaches of Protocol."
