# Heritage Committer

This skill governs the creation of "High-Fidelity" commits within the `qtr-qth` ecosystem. It ensures that every change is cryptographically signed, properly categorized, and thoroughly documented.

## 🏹 The Prime Directives

### 1. Pre-Commit Verification (Doctor's Orders)
Before initiating any commit, the agent MUST invoke the logic of the `heritage-doctor` skill:
- Verify `commit.gpgsign` is `true`.
- Verify `gpg.format` is `ssh`.
- Ensure the `user.signingkey` is present and the `allowed_signers` file is configured for local audit.

### 2. Semantic Structuralism (Conventional Commits)
All commits MUST follow the Conventional Commits specification:
`<type>(<scope>): <description>`
- **Types:** `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `release`.
- **Scope:** Mandatory; identifying the module (e.g., `nmea`, `serial`, `ntp`).
- **Description:** Imperative mood, lowercase, no trailing period.

### 3. The JARVIS Verbosity Standard
Ignore standard "caveman" compression rules for commit messages. Generate a detailed, British-inspired technical summary including:
- **Header:** The semantic headline.
- **Rationale:** A "Why, not what" explanation of the change.
- **Impact Analysis:** A bulleted list of modified files and architectural side-effects.

## 🛠️ Execution Workflow

1.  **Stage:** Use `git add` for specific, surgical files.
2.  **Audit:** Run `git status` and `git diff --staged` to confirm the payload.
3.  **Draft:** Generate the verbose commit message.
4.  **Execute:** `git commit -S -m "[headline]" -m "[body]"`
5.  **Verify:** `git log --show-signature -1` to confirm the "Good signature" status.

## 🚀 Guardrail
If signing fails or the environment is found to be "unhealthy" by the `heritage-doctor`, the committer MUST stop and perform a repair before proceeding. Never bypass the cryptographic seal.
