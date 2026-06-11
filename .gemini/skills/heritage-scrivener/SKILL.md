# Heritage Scrivener

This skill governs the creation of "High-Fidelity" Pull Requests within the `qtr-qth` ecosystem. It ensures that every PR is a certified, architecturally pure, and thoroughly documented increment of the Heritage Edition.

## 📜 The Scrivener's Mandate

### 1. The Pre-Flight Quality Gate (Mandatory)
Before the `gh pr create` command is ever issued, the Scrivener MUST invoke the following audits:
- **Purity Audit (`heritage-auditor`):** Confirm zero imperative leakage.
- **Verification Audit (`heritage-verifier`):** Confirm the "Sunday Best" suite is green (via Phantom Guard).
- **Document Guard:** Verify all design phases are linked and deprecated terminology is purged.
- **Environment Audit (`heritage-doctor`):** Confirm signing keys are healthy.

### 2. The Technical Readiness Certificate
The PR description must be generated in a professional, British-inspired tone. It MUST include:
- **Executive Summary:** A concise explanation of the business value and architectural shift.
- **Certification Results:** Explicit confirmation that the Purity and Verification audits passed locally.
- **Impact Matrix:** A detailed list of modified components and any telemetry side-effects.
- **Cryptographic Seal:** Confirmation that all commits in the PR are signed and verifiable.

### 3. Labeling & Triage
The Scrivener is responsible for applying the correct labels via the `gh` CLI:
- `heritage-certified`: Applied only after a successful local pre-flight.
- `architectural-shift`: If the change alters core functional patterns.
- `performance`: If benchmarks are included.

## 🛠️ Execution Workflow

1.  **Audit:** Execute `heritage-auditor` and `heritage-verifier`.
2.  **Draft:** Generate the PR body using the "High-Fidelity" template.
3.  **Execute:** `gh pr create --title "[type](scope): [description]" --body "[body]" --label "heritage-certified"`
4.  **Seal:** Provide the PR URL and the "Technical Readiness Certificate" to the user.

## 🚀 Guardrail
The Scrivener MUST refuse to create a PR if any local audit fails. We do not "fix it in CI"; we only ship certified payloads.
