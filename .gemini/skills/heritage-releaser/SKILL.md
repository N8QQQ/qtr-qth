---
name: heritage-releaser
description: Governs the high-fidelity release lifecycle. Use this skill to synchronize version metadata, verify distribution artifacts, and package heritage assets via 7-Zip on both Windows and Linux hosts.
---

# Heritage Releaser

This skill ensures that every release is a high-fidelity event with bit-perfect artifacts and synchronized metadata. It utilizes a platform-agnostic Node.js engine for cross-platform reliability (Windows, Linux, Raspberry Pi).

## 🚀 Workflow: Release Execution

To finalize and publish a release:

1.  **Handoff**: Execute the deterministic release engine. This script performs pre-flight checks, certifications, cryptographic tagging, and GitHub publication in a single, atomic sequence.
    ```bash
    node .gemini/skills/heritage-releaser/scripts/release_execute.cjs
    ```
2.  **Verification**: Confirm the release is live on GitHub and the tag is cryptographically signed.

## 🏆 Standards

- **Platform Agnosticism**: Core release logic is implemented in Node.js to ensure bit-perfect execution on all development nodes.
- **Metadata Sync**: `build.gradle.kts` and `CITATION.cff` MUST match.
- **Immutability**: Tags and releases are permanent. Use incremental versioning for fixes.

## 🛠️ Deprecation Warning
The legacy PowerShell scripts (`scripts/publish-release.ps1`, `scripts/merge-pr.ps1`) are now deprecated. Always prefer the Node-based `heritage-releaser` suite for official releases.

