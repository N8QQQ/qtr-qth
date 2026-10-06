<!-- BEGIN FLOW-NEXT -->
<!-- flow-next:snippet:v2 -->
## Flow-Next

This project uses Flow-Next for ALL task tracking. `flowctl` comes from the flow-next plugin install — every flow-next skill resolves it itself, and on Claude Code it is also on PATH. Do NOT create markdown TODOs or use TodoWrite. Cold session: `flowctl brief` first — one bounded call (specs, ready tasks, memory); go deeper with `show`/`cat`/`anchor <task-id>`.

- Lifecycle: `flowctl list` / `show fn-N.M` / `start fn-N.M` / `done fn-N.M --summary-file s.md --evidence-json e.json` (e.json: `{"commits": ["<sha>"], "tests": ["<cmd>"], "prs": []}`)
- BEFORE any other flowctl operation, or when unsure of a flag: run `flowctl usage` (CLI cheatsheet + orchestration recipes) or `flowctl --help`.
- BEFORE bridging work to another model/CLI (`codex exec`, `cursor-agent`, `claude -p`, `grok`) or picking an implementation/review model: run `flowctl usage` and follow "Orchestration & model steering" exactly.
- Creating a spec: write it directly — `$flow-next-plan` is task breakdown only. `flowctl spec create --title "Short title" --plan-file plan.md --json`, then `$flow-next-plan <spec-id>`. Scaffold cascade (first match wins): `SPEC.md` -> `spec.md` -> bundled template.
- Substantial replies (reports, reviews, multi-section answers): invoke `$flow-next-prose` BEFORE drafting — the artifact prose contract applies to chat replies too. Short conversational turns skip it.
- If `flowctl` is not found: your shell lacks the plugin's `scripts/` dir on PATH (only Claude Code injects it). Resolve it the way the skills do - the plugin install's `scripts/flowctl` (Claude/Droid: plugin-root env var; Codex: `${CODEX_HOME:-$HOME/.codex}/scripts/flowctl`; Cursor/Grok: two levels above any flow-next SKILL.md) - or update/reinstall the flow-next plugin. A repo with no `.flow/` yet: run `$flow-next-setup`.
<!-- END FLOW-NEXT -->

## Repository Context & Commands

This is a CLI/Library built with Java and Gradle.

### Repository Topology (Sovereign Development Model)
This repository follows the **Sovereign Inner Loop / Curated Delivery** pattern:
- **`origin` (Athena Forgejo):** The canonical working and development repository (`ssh://git@athena.hive.stoicbee.com:2223/nicholas/qtr-qth.git`). All day-to-day feature branches, PRs, internal issues (managed via `tea`), and local CI occur here.
- **`github` (GitHub):** The public delivery, release, and showcase mirror (`git@github.com:n8qqq/qtr-qth.git`). Public releases and milestone deliverables are synced downstream here.

### Common Commands
To build the project:
```bash
./gradlew build
```

To run tests:
```bash
./gradlew test
```

To run linting/checks:
```bash
./gradlew check
```

For hardware testing, use the WSL USB bridge script:
```bash
./scripts/wsl-usb-bridge.sh release
```

### CI / CD
- **Forgejo Actions:** Automated CI runs natively on Athena hardware via `.forgejo/workflows/ci.yaml` at zero cloud runner cost.
- **Local Container CI:** Run the full containerized scan suite (Super-Linter, CodeQL, Gitleaks, Trivy) on demand:
```bash
./scripts/local-ci.sh --all
```
- **Cloud CI:** Do not configure GitHub Actions for continuous iteration builds to avoid GitHub runner charges.

### Branching & Delivery Strategy
1. **Inner Loop (Forgejo):**
   - Active feature and refactoring branches (`fn-*`, `feat/*`, `fix/*`) are developed and merged against Forgejo `origin/main`.
   - Forgejo Actions automatically verify all commits and PRs.
2. **Outer Loop (GitHub Delivery):**
   - When a milestone or release is finalized on `origin/main`, verified changes and tags are delivered downstream to `github/main` (`git push github main --tags`).
   - The GitHub `main` branch rules enforce signed commits and release consistency. Non-fast-forward updates to `github/main` remain blocked.

### Release Process & Identity Tracking
The project uses academic/scientific identity tracking via **ORCID** (`0009-0001-9211-8000`).
When preparing a release, the agent **MUST** synchronize the version number across all tracking files simultaneously to avoid burning a point release on metadata fixes. A release MUST update:
1. `build.gradle.kts` (or `gradle.properties`)
2. `CITATION.cff`
3. `.zenodo.json`
Do not tag or release until all three identity and version artifacts are perfectly aligned.
