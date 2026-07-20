## Agent skills

### Issue tracker

Issues and PRDs live in this repository's GitHub Issues, accessed via the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Using standard triage labels (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context documentation layout at the repository root (`CONTEXT.md` + `docs/adr/`). See `docs/agents/domain.md`.

### CI/CD & Pre-Flight Checks

**CRITICAL RULE:** Agents must *always* execute the full local CI suite (`./scripts/local-ci.sh`) before pushing any commits to GitHub. We must catch our own errors locally to ensure the repository remains strictly stable. Do not push unverified code.

### Git Commit Signing

**CRITICAL RULE:** All commits must be cryptographically signed via SSH/GPG. Because agents run inside an isolated sandbox that lacks access to the host's `~/.ssh/` directory, any `git commit` operations performed by the agent MUST be executed with sandbox bypass enabled (e.g., `BypassSandbox: true` in the tool call) to access the user's host credentials. Do not use `--no-gpg-sign` to bypass errors.
