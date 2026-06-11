---
name: heritage-merger
description: Governs the atomic handoff and merge of Pull Requests. Use this skill to verify branch synchronization, identify active PR IDs, and generate certified merge sequences for both Windows and Linux hosts.
---

# Heritage Merger

This skill ensures that every merge is an atomic, certified event that preserves the high-fidelity history of the project.

## 🛡️ Workflow: Merge Certification

To finalize a phase and merge into `main`:

1.  **Verification**: Ensure the branch has passed `heritage-verifier`.
2.  **Execution**: Run the deterministic merge controller.
    ```bash
    node <path-to-skill>/scripts/merge_certified.cjs
    ```
3.  **Handoff**: Execute the generated merge sequence (e.g., `merge-pr.ps1`) provided in the Readiness Report.

## 🏆 Standards

- **Atomic Merges**: Only merge when technical purity is verified.
- **PR Identification**: Automatically detects PR IDs via `gh` CLI to prevent manual ID entry errors.
- **Post-Merge Sync**: Always return to `main` and pull immediately to ensure a clean baseline for the next phase.
