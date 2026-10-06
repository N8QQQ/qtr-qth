---
satisfies: [R2, R3, R4]
---
# fn-2-standardize-legacy-tests-on-given-when.4 Extract OrchestratorFixture and refactor system orchestrator and CLI integration tests

## Description
Create a shared `OrchestratorFixture` in package `com.stoicprogrammer.qtrqth` at `src/test/java/com/stoicprogrammer/qtrqth/OrchestratorFixture.java` to eliminate duplicated mock orchestrator wiring without violating package privacy, and refactor system orchestrator, recovery, stress, and CLI integration tests with domain-appropriate bounded timeouts.

**Size:** M
**Files:** src/test/java/com/stoicprogrammer/qtrqth/OrchestratorFixture.java, src/test/java/com/stoicprogrammer/qtrqth/SystemOrchestratorTest.java, src/test/java/com/stoicprogrammer/qtrqth/SystemIntegrationTest.java, src/test/java/com/stoicprogrammer/qtrqth/SystemRecoveryIntegrationTest.java, src/test/java/com/stoicprogrammer/qtrqth/ReactiveStressTest.java, src/test/java/com/stoicprogrammer/qtrqth/MainDoctorIntegrationTest.java, src/test/java/com/stoicprogrammer/qtrqth/MainIntegrationTest.java, src/test/java/com/stoicprogrammer/qtrqth/MainProbeIntegrationTest.java
**Touches:** [src/test/java/com/stoicprogrammer/qtrqth/OrchestratorFixture.java, src/test/java/com/stoicprogrammer/qtrqth/SystemOrchestratorTest.java, src/test/java/com/stoicprogrammer/qtrqth/SystemIntegrationTest.java, src/test/java/com/stoicprogrammer/qtrqth/SystemRecoveryIntegrationTest.java, src/test/java/com/stoicprogrammer/qtrqth/ReactiveStressTest.java, src/test/java/com/stoicprogrammer/qtrqth/MainDoctorIntegrationTest.java, src/test/java/com/stoicprogrammer/qtrqth/MainIntegrationTest.java, src/test/java/com/stoicprogrammer/qtrqth/MainProbeIntegrationTest.java]

### Approach
- Create `src/test/java/com/stoicprogrammer/qtrqth/OrchestratorFixture.java` in package `com.stoicprogrammer.qtrqth`. Because it resides in the same package as `SystemOrchestrator`, it can access the package-private dependency injection constructor `SystemOrchestrator(ConfigManager, ISerialProvider, INtpProvider, InstantSource, IStreamSentinel)` without modifying production code, and preserves strict acyclic package dependencies.
- Refactor `SystemOrchestratorTest`, `SystemIntegrationTest`, and `SystemRecoveryIntegrationTest` to use `OrchestratorFixture`.
- In `SystemRecoveryIntegrationTest`, use a 10-second `CountDownLatch` timeout to accommodate `SerialConnector.WATCHDOG_TIMEOUT_SECONDS = 5` and `SystemOrchestrator.RECOVERY_BACKOFF_MS = 2000`.
- In `ReactiveStressTest`, refactor high-throughput event loops to use `CountDownLatch` with 30-second timeout (accommodating 100–200 pulse bursts at 25/50Hz) and inner `StressFixture`.
- In `MainDoctorIntegrationTest`, `MainIntegrationTest`, and `MainProbeIntegrationTest`, encapsulate CLI command executions and output checks into inner `CliFixture` classes.
- Verify with `./gradlew test --tests "*System*" --tests "*Main*IntegrationTest*" --tests "*ReactiveStressTest*"`.

### Investigation targets
**Required:**
- `src/main/java/com/stoicprogrammer/qtrqth/SystemOrchestrator.java:70-82` — package-private testing constructor to invoke
- `src/test/java/com/stoicprogrammer/qtrqth/SystemOrchestratorTest.java` — current orchestrator wiring and assertion duplication
- `src/test/java/com/stoicprogrammer/qtrqth/SystemIntegrationTest.java` — system integration test setup
- `src/test/java/com/stoicprogrammer/qtrqth/SystemRecoveryIntegrationTest.java` — recovery loop assertions (requires 10s timeout)
- `src/test/java/com/stoicprogrammer/qtrqth/ReactiveStressTest.java` — stress burst assertions (requires 30s timeout)

### Key context
`OrchestratorFixture` must provide complete isolation between test methods by resetting all mocks and queues upon each `given_` invocation. Concurrency latch timeouts are strictly bounded and non-zero: 10s for recovery, 30s for stress.

## Acceptance
- [ ] `src/test/java/com/stoicprogrammer/qtrqth/OrchestratorFixture.java` is implemented in package `com.stoicprogrammer.qtrqth` and encapsulates common orchestrator mock plumbing.
- [ ] `SystemOrchestratorTest`, `SystemIntegrationTest`, and `SystemRecoveryIntegrationTest` delegate assertions to `OrchestratorFixture`.
- [ ] `SystemRecoveryIntegrationTest` executes deterministically with 10s bounded timeout.
- [ ] `ReactiveStressTest` uses bounded 30s `CountDownLatch` synchronization without thread sleeps.
- [ ] `MainDoctorIntegrationTest`, `MainIntegrationTest`, and `MainProbeIntegrationTest` encapsulate CLI execution assertions in `CliFixture`.
- [ ] All refactored integration tests pass with `./gradlew test`.

## Done summary
TBD

## Evidence
- Commits:
- Tests:
- PRs:
