---
name: heritage-verifier
description: The absolute quality standard for qtr-qth. Use this skill to certify code changes via the deterministic 'Sunday Best' suite. Orchestrates tests, checkstyle, and technical purity audits on both Athena (Windows) and Gandalf/Mechanar (Linux).
---

# Heritage Verifier

This skill orchestrates the exhaustive verification of the system's structural and functional integrity.

## 🛡️ Workflow: Technical Readiness Certification

To certify a branch or PR:

1.  **Execution**: Run the deterministic health controller. This script now **mandates** the use of the `phantom` container to guarantee bit-perfect parity with GitHub Actions.
    ```bash
    node <path-to-skill>/scripts/verify_health.cjs
    ```
2.  **Analysis**: Review the **Technical Readiness Certificate** output.

## 🏆 Success Criteria

A successful certification must return:
- **Test Status**: [PASSED] (100% pass rate).
- **Style Compliance**: [PASSED] (0 Violations).
- **Technical Purity**: [Passed] (No imperative leakage, including Raw JDK Parsing).
- **Coverage Depth**: [PASSED] (>90% instructions covered).

## 🐋 Phantom Guard (Primary Gate)

The system now defaults to the `phantom` container for all local checks. This ensures the codebase handles LF line endings, Linux pathing, and strict architectural lints identically to the CI runner.

