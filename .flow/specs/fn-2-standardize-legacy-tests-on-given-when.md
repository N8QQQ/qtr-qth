# fn-2-standardize-legacy-tests-on-given-when Standardize legacy tests on Given-When-Then BDD fixture pattern

## Goal & Context
<!-- scope: business -->

Retrofit the repository test suite to satisfy the Given-When-Then BDD Fixture standard mandated by repository architectural guidelines and tracked in GitHub Issue #79 and Forgejo Issue #7. Historically, 15 legacy test files (plus 5 loose assertions in `NmeaParserTest`) relied on loose assertions scattered directly across test method bodies or plain `BddTest` logging methods without structured fixture encapsulation. This specification brings the entire test suite into 100% compliance by eliminating loose assertions, removing dead scratch tests, establishing reusable fixtures for complex orchestrator subsystems, and introducing an automated Checkstyle gate to prevent regressions.

## Quick commands
```bash
# Check test assertion boundaries and execute unit tests
./gradlew checkstyleTest
./gradlew test
```

## Architecture & Data Models
<!-- scope: technical -->

The test architecture standardizes on the **Fixture Model Pattern**:
1. **File-Local Fixtures:** Each standard unit test class defines an inner `*Fixture` (e.g. `private final class AnalyzerFixture`) encapsulating test state, inputs, mocks, and outputs. Test methods interact solely through:
   - `fixture.given_<precondition>()`
   - `fixture.when_<action>()`
   - `fixture.then_<assertion>()`
2. **Shared Subsystem Fixtures:** Complex multi-component integration harnesses (specifically `OrchestratorFixture`) are placed in package `com.stoicprogrammer.qtrqth` at `src/test/java/com/stoicprogrammer/qtrqth/OrchestratorFixture.java`. This preserves access to `SystemOrchestrator`'s package-private constructor for mock dependency injection, maintains a strict acyclic DAG, and eliminates duplicated mock configuration across `SystemOrchestratorTest`, `SystemIntegrationTest`, and `SystemRecoveryIntegrationTest`.
3. **Reactive Concurrency Control:** Integration fixtures managing asynchronous serial streams or scheduled background executors employ `CountDownLatch` instances with tiered bounded timeouts:
   - Unit asynchronous fixtures (`ExecutorSentinelTest`, `ReactiveInversionTest`): strict 3-second timeout.
   - Watchdog recovery integration fixtures (`SystemRecoveryIntegrationTest`): 10-second timeout (accommodating the 5s serial silence watchdog and 2s recovery backoff).
   - High-throughput stress fixtures (`ReactiveStressTest`): 30-second timeout (accommodating 100–200 pulse bursts at 25/50Hz).
   All latch assertions use descriptive AssertJ statements: `assertThat(latch.await(timeout, unit)).as("Timed out waiting for %s after %d %s", event, timeout, unit).isTrue();`.

## API Contracts
<!-- scope: technical -->

Fixture methods must adhere to strict snake_case naming conventions:
- Preconditions: `void given_<description>([parameters])`
- Actions / Executions: `void when_<operation>([parameters])`
- Postconditions / Assertions: `void then_<expectation>([parameters])`

All AssertJ assertions (`assertThat`) must reside strictly inside `Fixture` classes. Test methods themselves must contain zero direct assertion statements.

## Edge Cases & Constraints
<!-- scope: technical -->

- **Test Isolation:** Shared fixtures must reset state between test method invocations (either per-method instantiation or explicit reset).
- **Timeout Discipline:** Asynchronous latch waits must follow the tiered limits (3s unit, 10s recovery, 30s stress); on expiration, fixtures must emit clear assertions identifying the missing event.
- **Dead Code Pruning:** Obsolete scratch files `src/test/java/com/stoicprogrammer/qtrqth/serial/MockitoCheckTest.java` and `src/test/java/com/stoicprogrammer/qtrqth/serial/SimpleMockTest.java` must be deleted completely.
- **Production Code Immutability:** No production code in `src/main/java/` may be altered to accommodate test fixtures; tests must exercise existing public or package-private APIs cleanly.
- **Checkstyle Gate Sequencing:** To prevent breaking the build while legacy tests are refactored, the Checkstyle `MatchXPath` rule is activated in Wave 3 (`fn-2.5`) after all test suites in Wave 2 are fully compliant.

## Boundaries
<!-- scope: business -->

- **Out of Scope (Production Logic):** No modification of core runtime packages (`model`, `analysis`, `nmea`, `serial`, `sentinel`, `ntp`, `util`).
- **Out of Scope (Test Framework Migration):** JUnit 5 and AssertJ remain the exclusive test dependencies; no introduction of Cucumber, Spock, or other external BDD frameworks.
- **Out of Scope (CI Engine):** CI remains local; no cloud CI GitHub Actions workflows are added.

## Decision Context
<!-- scope: both — conditionally substructured -->

### Motivation
Standardizing test structures eliminates brittle, unstructured assertions and aligns the entire repository with the strict BDD criteria required by our internal review protocols (`nru-expert-review`). It directly resolves GitHub Issue #79 and Forgejo Issue #7.

### Implementation Tradeoffs
- **OrchestratorFixture Package Placement:** Placed `OrchestratorFixture` in package `com.stoicprogrammer.qtrqth` at `src/test/java/com/stoicprogrammer/qtrqth/OrchestratorFixture.java` rather than `com.stoicprogrammer.qtrqth.base` to maintain access to `SystemOrchestrator`'s package-private constructor and prevent cyclic package dependencies between `com.stoicprogrammer.qtrqth` and `base`.
- **Checkstyle Enforcement Mechanism & Sequencing:** Utilized Checkstyle's `MatchXPath` under `TreeWalker` (`//METHOD_CALL[.//IDENT[@text='assertThat']][not(ancestor::CLASS_DEF[contains(./IDENT/@text, 'Fixture')])]`). Sequenced active enforcement to Wave 3 (`fn-2.5`) to keep the build green across all intermediate steps.
- **Tiered Concurrency Timeouts:** Parameterized latch timeouts to respect domain invariants (3s for unit fixtures, 10s for serial watchdog neutralization, 30s for high-frequency stress bursts) while strictly eliminating arbitrary sleeps.

## Acceptance Criteria
<!-- scope: both -->

- **R1:** Eliminate redundant scratch tests by deleting `MockitoCheckTest.java` and `SimpleMockTest.java`. [user]
  Errors: no error surface beyond file removal.
- **R2:** Refactor all 15 remaining legacy test suites plus loose assertions in `NmeaParserTest` to encapsulate assertions within structured inner `Fixture` classes following `fixture.given_...()`, `fixture.when_...()`, `fixture.then_...()` snake_case naming. [user]
  Errors: no error surface beyond test failure reporting.
- **R3:** Extract shared multi-test orchestration test harness `OrchestratorFixture` into `com.stoicprogrammer.qtrqth` at `src/test/java/com/stoicprogrammer/qtrqth/OrchestratorFixture.java` to eliminate duplicate mock setups across `SystemOrchestratorTest` and `SystemIntegrationTest` without violating package privacy. [user]
  Errors: no error surface beyond test isolation.
- **R4:** Asynchronous and reactive stream test fixtures must utilize `CountDownLatch` with tiered bounded timeouts (3s unit, 10s recovery, 30s stress) using fluent AssertJ assertions (`assertThat(latch.await(timeout, unit)).as(...).isTrue()`). [user]
  Errors: timeout exceeded -> explicit AssertionError with descriptive failure message.
- **R5:** Configure Checkstyle via `MatchXPath` under `TreeWalker` to enforce that AssertJ assertions (`assertThat`) are exclusively invoked within classes ending in `Fixture`, verified in Wave 3 (`fn-2.5`). [user]
  Errors: Checkstyle violation -> non-zero build exit with filename and line number.
- **R6:** Verify that all test suites pass cleanly via `./gradlew test` and full repository checks succeed under `./gradlew check`. [paraphrase]
  Errors: test or lint failure -> non-zero build exit.

## Early proof point
Task fn-2.1 validates scratch test pruning and documents the BDD fixture pattern in developer docs. Task fn-2.4 validates the shared `OrchestratorFixture` wiring against package-private constructors.

## Requirement coverage

| Req | Description | Task(s) | Gap justification |
|-----|-------------|---------|-------------------|
| R1  | Eliminate scratch tests (`MockitoCheckTest`, `SimpleMockTest`) | fn-2.1 | — |
| R2  | Refactor 15 legacy test suites + `NmeaParserTest` to inner `Fixture` classes | fn-2.2, fn-2.3, fn-2.4 | — |
| R3  | Extract shared `OrchestratorFixture` to `com.stoicprogrammer.qtrqth` | fn-2.4 | — |
| R4  | Enforce tiered bounded timeouts with `CountDownLatch` in async/reactive fixtures | fn-2.3, fn-2.4 | — |
| R5  | Configure Checkstyle `MatchXPath` rule enforcing `assertThat` in `*Fixture` classes | fn-2.5 | — |
| R6  | Verify full test suite and build quality gates pass cleanly | fn-2.5 | — |

## Resolved via Codebase
- Confirmed existing fixture reference implementations in `NmeaSentenceAccumulatorTest.java`, `AppConfigTest.java`, and `TelemetryPulseTest.java`.
- Audited test suite: identified 15 active legacy test files requiring fixture retrofitting, 5 loose assertions in `NmeaParserTest.java`, and 2 scratch files (`MockitoCheckTest`, `SimpleMockTest`) for removal.
- Validated `SystemOrchestrator` constructor visibility (package-private in `com.stoicprogrammer.qtrqth`).
- Verified `SerialConnector.WATCHDOG_TIMEOUT_SECONDS = 5` and `SystemOrchestrator.RECOVERY_BACKOFF_MS = 2000` requiring 10s timeout in recovery integration tests.

## Resolved via Project Docs
- Aligned requirements with `.agents/skills/nru-expert-review/SKILL.md` (Phase 3: Fixture-Driven BDD).
- Aligned scope with GitHub Issue #79 and Forgejo Review Issue #7.
- Verified test infrastructure boundaries in `docs/DEVELOPER.md`.

## References
- GitHub Issue #79
- Forgejo Review Issue #7
- Memory entry: `.flow/memory/knowledge/decisions/anchor-architectural-redesign-against-2026-10-03.md`
- Developer guide: `docs/DEVELOPER.md`
- Checkstyle configuration: `config/checkstyle/checkstyle.xml`
