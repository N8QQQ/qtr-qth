## Agent skills

### Issue tracker

Internal engineering issues, tasks, and refactoring PRDs live in Athena Forgejo, accessed via the `tea` CLI (`tea issues --login athena-forge --repo nicholas/qtr-qth`). Public issues and milestone releases are mirrored/accessed via GitHub (`gh` CLI).

### Triage labels

Using standard triage labels (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`).

### Domain docs

Single-context documentation layout at the repository root (`CONTEXT.md` + `docs/adr/`).

### CI/CD & Pre-Flight Checks

**CRITICAL RULE:** Agents must *always* verify tests and builds locally (`./gradlew check`) or via Athena Forgejo Actions (`.forgejo/workflows/ci.yaml`) before pushing commits, and run the full local CI suite (`./scripts/local-ci.sh`) before tagging or delivering releases downstream to GitHub. Do not push unverified code.

### Git Commit Signing

**CRITICAL RULE:** All commits must be cryptographically signed via SSH/GPG. Because agents run inside an isolated sandbox that lacks access to the host's `~/.ssh/` directory, any `git commit` operations performed by the agent MUST be executed with sandbox bypass enabled (e.g., `BypassSandbox: true` in the tool call) to access the user's host credentials. Do not use `--no-gpg-sign` to bypass errors.
