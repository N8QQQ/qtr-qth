## Agent skills

### Issue tracker

Issues and PRDs live in this repository's GitHub Issues, accessed via the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Using standard triage labels (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context documentation layout at the repository root (`CONTEXT.md` + `docs/adr/`). See `docs/agents/domain.md`.

### CI/CD & Pre-Flight Checks

**CRITICAL RULE:** Agents must *always* execute the full local CI suite (`./scripts/local-ci.sh`) before pushing any commits to GitHub. We must catch our own errors locally to ensure the repository remains strictly stable. Do not push unverified code.
