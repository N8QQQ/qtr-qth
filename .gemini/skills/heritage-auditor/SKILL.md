---
name: heritage-auditor
description: Performs strategic codebase mapping and technical purity audits. Use this skill to quantify imperative leakage, map architectural dependencies, and identify technical debt across both Windows and Linux hosts.
---

# Heritage Auditor

This skill provides the strategic "Map of the Battlefield," identifying where the codebase adheres to the Heritage Protocol and where refactoring is required.

## 🛰️ Workflow: Strategic Audit

To quantify the project state:

1.  **Execution**: Run the deterministic project audit controller.
    ```bash
    node <path-to-skill>/scripts/project_audit.cjs
    ```
2.  **Analysis**: Review the **Architecture Map** and **Technical Purity Audit** results.
3.  **Triage**: Prioritize refactoring targets based on the "Imperative Leakage" counts.

## 🏆 Standards

- **Low-Token Mapping**: Replaces manual `grep` and `find` commands with a single, high-fidelity script pass.
- **Cross-Platform Parity**: Identifies pathing or casing anomalies between Windows and Linux module definitions.
- **Acyclic Enforcement**: Audits for package-level circular dependencies.
