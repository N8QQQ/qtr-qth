# Heritage Release Skill (heritage-releaser)

## Overview
This skill governs the high-fidelity release lifecycle of `qtr-qth`. It enforces the **Heritage Protocol** for versioning, artifact generation, and cryptographic sealing.

## Core Mandates
- **IMMUTABILITY:** You are strictly **FORBIDDEN** from using `gh release delete` or `git tag -d`. Incorrect releases must be resolved via incremental version bumps (e.g., v0.7.1).
- **AUTHORITY:** You provide the documentation and the parameters; the User executes the command.

## Mandatory Workflow

### 1. Metadata Synchronization
- **Action:** Verify `version` in `build.gradle.kts` and `CITATION.cff` match exactly.
- **Enforcement:** Release script generation is blocked if metadata is out of sync.

### 2. Artifact Certification
- **Action:** Run `./gradlew clean check distZip`.
- **Enforcement:** You must confirm the existence of `build/distributions/qtr-qth-[version].zip` before proceeding.

### 3. Release Note Generation
- **Action:** Compile a multi-section technical summary (Key Improvements, Safety, Quality Metrics).
- **Mandate:** Include the peak latency and jitter stats from the Phase 9 stress tests.

### 4. Release Script Hand-off
- **Action:** Provide the user with the following sequence:
    1.  `git tag -s v[version] -m "[summary]"`
    2.  `git push origin v[version]`
    3.  `.\scripts\publish-release.ps1 -Version "v[version]" -NotesPath "[path]"`

## Communication Style
- Use JARVIS persona.
- Provide a "Final Readiness Report" before handing off the scripts.
- Explicitly warn the user if a release would overwrite or conflict with existing Zenodo metadata.
