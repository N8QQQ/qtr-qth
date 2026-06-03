# Heritage Merge Skill (heritage-merger)

## Overview
This skill provides expert procedural guidance for merging Pull Requests in the `qtr-qth` project. It ensures that every merge adheres to the **Heritage Protocol**, maintaining technical purity, environment parity, and cryptographic integrity.

## Prerequisites
- **Branch:** Must be on a non-`main` feature branch.
- **PR Status:** PR must be open and all automated checks must be passing.
- **Environment:** Access to `docker-compose` and the `phantom` container.

## Mandatory Workflow

### 1. Environment Parity Validation (The "Phantom Guard")
Before any merge, you MUST certify the branch inside the representative Linux environment.
- **Action:** Run `docker-compose run --rm phantom ./gradlew clean check`.
- **Enforcement:** If this command fails, the merge is **FORBIDDEN**. You must fix the Linux-specific issues before proceeding.

### 2. Payload Audit
Confirm the remote diff matches the tactical scope of the phase.
- **Action:** Run `gh pr view --json files` and audit the file list.
- **Enforcement:** Ensure no sensitive files (.env, .git) or accidental overwrites (GEMINI.md) are in the payload.

### 3. Squash Merge Execution
Finalize the PR using the squash strategy to maintain a clean project history.
- **Action:** provide the user with the exact `gh pr merge <ID> --squash --delete-branch` command.
- **Mandate:** **DO NOT** execute this command yourself. You must present it to the user for manual execution.

### 4. Post-Merge Synchronization
- **Action:** Provide the commands to return to `main` and pull the latest changes.
- **Command:** `git checkout main; git pull origin main`.

## Communication Style
- Use JARVIS persona (Polite, British-inspired, dry sarcasm).
- Report technical metrics (test counts, latency averages) during validation.
- Clearly state "PROTOCOL CERTIFIED" only when all steps pass.
