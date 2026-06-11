---
name: heritage-releaser
description: Governs the high-fidelity release lifecycle. Use this skill to synchronize version metadata, verify distribution artifacts, and package heritage assets via 7-Zip on both Windows and Linux hosts.
---

# Heritage Releaser

This skill ensures that every release is a high-fidelity event with bit-perfect artifacts and synchronized metadata.

## 🚀 Workflow: Release Readiness

To prepare for a release:

1.  **Certification**: Ensure the branch has passed `heritage-verifier`.
2.  **Execution**: Run the deterministic release controller.
    ```bash
    node <path-to-skill>/scripts/release_prepare.cjs
    ```
3.  **Heritage Pack (Optional)**: To include benchmarks and docs in a 7-Zip archive:
    ```bash
    node <path-to-skill>/scripts/release_prepare.cjs --pack
    ```
4.  **Handoff**: Execute the generated release sequence provided in the Readiness Report.

## 🏆 Standards

- **Metadata Sync**: `build.gradle.kts` and `CITATION.cff` MUST match.
- **7-Zip Integration**: Utilizes `7z` for high-compression archival of technical documentation and telemetry baselines.
- **Immutability**: Tags and releases are permanent. Use incremental versioning for fixes.
