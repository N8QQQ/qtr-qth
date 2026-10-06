---
title: Application Model and Architectural Contention Points
date: "2026-10-05"
focus_hint: We have the present qtr-qth work. I would like to develop the over all application model and framework we want to implement. Read the present project project source code as our startign point. Thne we need to architect a better system or at lease resolve circital contention points and create a workable software.
volume: 18
survivor_count: 9
rejected_count: 9
rejection_rate: 0.5
artifact_id: architect-application-model-framework-2026-10-05
promoted_ideas: [9]
promoted_to: {"9": [fn-2-standardize-legacy-tests-on-given-when]}
status: active
---

## Focus

Architect application model and framework for qtr-qth, resolving critical contention points:
- Decouple DAG module boundaries (extract CLI tools from util, leafify model package).
- Eliminate hot-path heap thrashing (Map.of / lambda allocations) in serial byte ingestion loops.
- Remediate temporal precision defects (midnight UTC boundary rollover, leap-second handling).
- Enforce OWASP security guardrails (parameterize ProcessBuilder, canonical path containment).
- Unify functional Vavr error containers and standardize BDD test fixtures.

## Grounding snapshot

git_log_30d: 67 files modified
top:
  - .agents/AGENTS.md
  - AGENTS.md
  - .agents/reports/codeql.sarif
  - .agents/reports/gitleaks.json
  - .agents/reports/trivy.json
  - .agents/skills/nru-expert-review/nru-functional-rules.md
  - .agents/skills/nru-expert-review/nru-owasp-rules.md
  - .agents/skills/nru-expert-review/SKILL.md
  - build.gradle.kts
  - .clawpatch/.gitignore

open_specs: 1
  - fn-1-add-badges-to-readme: Add badges to README

changelog_recent: scanned: none (no CHANGELOG.md)

memory_matches: 1
  - [knowledge/decisions] Anchor architectural redesign against 12 Forgejo review issues — tags: 

memory_audit_stale: scanned: none (audit not run)

strategy: scanned: none (no STRATEGY.md signal)

## Survivors

### High leverage (1-3)

#### 1. Eliminate Map.of heap thrashing in NmeaSentenceAccumulator byte loop
**Summary:** Replace branch-simulating Map.of allocations with direct branchless switch/state step in byte ingestion.
**Leverage:** Small-diff lever because modifications are confined to NmeaSentenceAccumulator.java; impact lands on GC throughput and eliminates per-byte heap allocations across continuous serial streams.
**Size:** S
**Affected areas:** src/main/java/com/stoicprogrammer/qtrqth/nmea/NmeaSentenceAccumulator.java
**Risk notes:** Must preserve exact sentence framing semantics across split and multi-chunk serial buffers.
**Persona:** senior-maintainer
**Next step:** /flow-next:refine

#### 2. Remediate midnight UTC day-boundary rollover offset explosion
**Summary:** Adjust OffsetAnalyzer calculation to handle GPS vs system time crossing the 00:00:00 UTC day boundary.
**Leverage:** Small-diff lever because logic is isolated entirely within OffsetAnalyzer.java; impact lands on clock offset discipline accuracy across the 00:00:00 UTC day boundary.
**Size:** S
**Affected areas:** src/main/java/com/stoicprogrammer/qtrqth/analysis/OffsetAnalyzer.java
**Risk notes:** Requires testing across positive and negative offsets near 23:59:59.999 and 00:00:00.001 UTC.
**Persona:** adversarial-reviewer
**Next step:** /flow-next:refine

#### 3. Replace Runtime.exec with parameterized ProcessBuilder in EnvironmentDoctor
**Summary:** Sanitize and parameterize system binary invocations (docker, compose) to eliminate injection vectors.
**Leverage:** Small-diff lever because changes touch only the process invocation method in EnvironmentDoctor.java; impact lands on system diagnostic security by eliminating arbitrary command execution vectors.
**Size:** S
**Affected areas:** src/main/java/com/stoicprogrammer/qtrqth/util/EnvironmentDoctor.java
**Risk notes:** Platform differences between Windows and Linux binary discovery (e.g. docker.exe vs docker).
**Persona:** adversarial-reviewer
**Next step:** /flow-next:refine

### Worth considering (4-7)

#### 4. Decouple DAG and extract EnvironmentDoctor to cli package
**Summary:** Move EnvironmentDoctor from util to cli and isolate interactive console outputs from core utilities.
**Leverage:** Small-diff lever because changes involve moving EnvironmentDoctor.java to a cli package and updating Main imports; impact lands on package architecture by restoring a strict Directed Acyclic Graph.
**Size:** S
**Affected areas:** src/main/java/com/stoicprogrammer/qtrqth/util/EnvironmentDoctor.java, src/main/java/com/stoicprogrammer/qtrqth/cli/
**Risk notes:** Requires updating Main routing rules and MainDoctorIntegrationTest import paths.
**Persona:** senior-maintainer
**Next step:** /flow-next:refine

#### 5. Support GPS leap-second (second 60) parsing in NmeaParser
**Summary:** Handle second=60 in NMEA time strings without throwing DateTimeException from LocalTime.of.
**Leverage:** Small-diff lever because adjustments are contained within extractTime in NmeaParser.java; impact lands on sentence ingestion resilience by preventing fatal parser crashes during UTC leap seconds.
**Size:** S
**Affected areas:** src/main/java/com/stoicprogrammer/qtrqth/nmea/NmeaParser.java
**Risk notes:** Java java.time.LocalTime strictly rejects second 60; requires a domain timestamp wrapper or normalization.
**Persona:** adversarial-reviewer
**Next step:** /flow-next:refine

#### 6. Enforce canonical path validation on configuration and capture output files
**Summary:** Guard file paths against directory traversal and verify parent directories before write operations.
**Leverage:** Small-diff lever because path containment checks are localized within ConfigManager and Main; impact lands on file I/O security by blocking directory traversal attacks.
**Size:** S
**Affected areas:** src/main/java/com/stoicprogrammer/qtrqth/config/ConfigManager.java, src/main/java/com/stoicprogrammer/qtrqth/Main.java
**Risk notes:** Must ensure valid handling of relative paths when run from different working directories.
**Persona:** adversarial-reviewer
**Next step:** /flow-next:refine

#### 7. Leafify model package by removing cross-layer dependencies from TelemetryPulse
**Summary:** Extract GridSquare calculation and PrecisionMetrics logging out of model record methods into a domain service.
**Leverage:** Small-diff lever because refactoring extracts formatting methods from the TelemetryPulse record into a logger service; impact lands on domain purity by turning the model package into an independent leaf.
**Size:** M
**Affected areas:** src/main/java/com/stoicprogrammer/qtrqth/model/TelemetryPulse.java, src/main/java/com/stoicprogrammer/qtrqth/SystemOrchestrator.java
**Risk notes:** Callers relying on pulse.logFinal() must be updated to use a dedicated TelemetryLogger service.
**Persona:** senior-maintainer
**Next step:** /flow-next:refine

### If you have the time (8+)

#### 8. Unify monadic functional types from java.util.Optional to Vavr Option/Try
**Summary:** Standardize all domain utilities and parsers on Vavr monadic containers, eliminating conversion bridges.
**Leverage:** Small-diff lever because refactoring standardizes existing method return types on Vavr types; impact lands on functional pipeline ergonomics across all parser and utility consumers.
**Size:** M
**Affected areas:** src/main/java/com/stoicprogrammer/qtrqth/util/Functional.java, src/main/java/com/stoicprogrammer/qtrqth/nmea/NmeaParser.java
**Risk notes:** Broad touch across public API method signatures in parser and utility packages.
**Persona:** senior-maintainer
**Next step:** /flow-next:refine

#### 9. Standardize legacy tests on Given-When-Then BDD fixture pattern
**Summary:** Refactor loose assertions in SystemOrchestratorTest and OffsetAnalyzerTest to strict fixture model.
**Leverage:** Small-diff lever because test assertions are rewritten into structured fixtures without changing production code; impact lands on test maintainability and verification consistency across the regression suite.
**Size:** M
**Affected areas:** src/test/java/com/stoicprogrammer/qtrqth/SystemOrchestratorTest.java, src/test/java/com/stoicprogrammer/qtrqth/analysis/OffsetAnalyzerTest.java
**Risk notes:** High line count changes in test files; logic must remain functionally equivalent.
**Persona:** senior-maintainer
**Next step:** /flow-next:refine

## Rejected

- Harden ExecutorSentinel against silent task suppression on uncaught exceptions — insufficient-signal: No grounding evidence or issue reports indicate that ExecutorSentinel suffers from uncaught exceptions terminating the loop.
- Eliminate per-sentence Map.of allocations in SerialConnector callback — insufficient-signal: Sentence queue offerings at 1-10 Hz do not present the hot-path allocation pressure identified in the byte accumulator.
- Add poison-pill queue event to gracefully unblock SerialConnector on shutdown — insufficient-signal: No grounding evidence indicates shutdown hangs or justifies introducing a poison-pill pattern into the queue pipeline.
- Prevent floating-point precision drift in StatisticalWindow running sums — insufficient-signal: Grounding evidence contains no report or issue regarding floating-point summation drift in StatisticalWindow.
- Replace regex NMEA sanitization with fast character-array validation — insufficient-signal: No profiling data or issue report in the grounding snapshot supports optimizing the regex replaceAll in NmeaParser.
- Introduce configurable serial watchdog timeout to prevent false recovery loops — insufficient-signal: Grounding snapshot contains no evidence showing false recovery triggers from the fixed 5-second watchdog timeout.
- Eliminate Map.of allocation in AutoBaudEngine character scan loop — insufficient-signal: AutoBaudEngine runs only transiently during initial hardware discovery, making heap allocation optimization there low-priority without telemetry evidence.
- Extract Command Router from Main into dedicated CLI command architecture — insufficient-signal: No grounding evidence supports replacing Main's simple 4-route dispatch table with an elaborate CLI command registry.
- Add hardware flow-control and DTR/RTS toggle configuration for GNSS receivers — out-of-scope: Introduces ungrounded hardware signaling features before completing the prerequisite architectural hardening roadmap in CONTEXT.md.
