---
satisfies: [R2]
---
# fn-2-standardize-legacy-tests-on-given-when.2 Refactor Analysis, Config, and Functional unit tests to inner Fixtures

## Description
Standardize unit tests across the analysis, config, and utility packages onto the Given-When-Then Fixture pattern by encapsulating state and AssertJ assertions into inner `Fixture` classes with snake_case methods.

**Size:** M
**Files:** src/test/java/com/stoicprogrammer/qtrqth/analysis/OffsetAnalyzerTest.java, src/test/java/com/stoicprogrammer/qtrqth/analysis/StatisticalWindowTest.java, src/test/java/com/stoicprogrammer/qtrqth/config/ConfigManagerTest.java, src/test/java/com/stoicprogrammer/qtrqth/util/FunctionalTest.java
**Touches:** [src/test/java/com/stoicprogrammer/qtrqth/analysis/OffsetAnalyzerTest.java, src/test/java/com/stoicprogrammer/qtrqth/analysis/StatisticalWindowTest.java, src/test/java/com/stoicprogrammer/qtrqth/config/ConfigManagerTest.java, src/test/java/com/stoicprogrammer/qtrqth/util/FunctionalTest.java]

### Approach
- In `OffsetAnalyzerTest`, create inner `AnalyzerFixture` encapsulating analyzer instance, sample inputs, and assertions (`given_baseline_offset`, `when_analyzing_drift`, `then_offset_matches`). Remove all direct `assertThat` calls from `@Test` methods.
- In `StatisticalWindowTest`, create inner `WindowFixture` encapsulating the statistical window, running updates, and variance/jitter assertions.
- In `ConfigManagerTest`, create inner `ManagerFixture` encapsulating configuration file paths, overrides, and environment property assertions.
- In `FunctionalTest`, create inner `FunctionalFixture` encapsulating monadic parsing and functional utility assertions.
- Verify using `./gradlew test --tests "*OffsetAnalyzerTest*" --tests "*StatisticalWindowTest*" --tests "*ConfigManagerTest*" --tests "*FunctionalTest*"`.

### Investigation targets
**Required:**
- `src/test/java/com/stoicprogrammer/qtrqth/config/AppConfigTest.java:40-67` — reference pattern for `ConfigFixture`
- `src/test/java/com/stoicprogrammer/qtrqth/analysis/OffsetAnalyzerTest.java` — current legacy assertions to encapsulate
- `src/test/java/com/stoicprogrammer/qtrqth/analysis/StatisticalWindowTest.java` — current legacy assertions to encapsulate
- `src/test/java/com/stoicprogrammer/qtrqth/config/ConfigManagerTest.java` — current legacy assertions to encapsulate
- `src/test/java/com/stoicprogrammer/qtrqth/util/FunctionalTest.java` — current legacy assertions to encapsulate

### Key context
Tests must strictly use `given_<state>()`, `when_<action>()`, and `then_<assertion>()` naming. Test methods must contain zero direct `assertThat` calls, satisfying the Checkstyle boundary rule.

## Acceptance
- [ ] `OffsetAnalyzerTest` contains zero direct `assertThat` calls in test methods and delegates to `AnalyzerFixture`.
- [ ] `StatisticalWindowTest` encapsulates all calculations and assertions inside `WindowFixture`.
- [ ] `ConfigManagerTest` encapsulates file handling and assertions inside `ManagerFixture`.
- [ ] `FunctionalTest` encapsulates monadic tests inside `FunctionalFixture`.
- [ ] All refactored tests pass with `./gradlew test`.

## Done summary
# Task fn-2.2 Summary: Refactor Analysis, Config, and Functional Unit Tests

### Accomplishments
- Refactored `OffsetAnalyzerTest` to inner `AnalyzerFixture` with snake_case given-when-then methods. Replaced magic numbers with `LocalTime.NOON`.
- Refactored `StatisticalWindowTest` to inner `WindowFixture`. Replaced imperative loops with functional stream reductions (`Arrays.stream(...).reduce(...)`).
- Refactored `ConfigManagerTest` to inner `ManagerFixture` with `Optional<ConfigManager.FileAction>` monadic composition, eliminating null checks.
- Refactored `FunctionalTest` to inner `FunctionalFixture` supporting both standard `@Test` and `@ParameterizedTest` methods.
- Eliminated 100% of direct assertions in `@Test` methods across all 4 files.
- Zero production files touched.
- All 69 tests pass cleanly; `checkstyleTest` passes with 0 violations.
- Adversarial Carmack implementation review completed with verdict `SHIP`.
## Evidence
- Commits: d11a0f911a37cce147817db445a9ba014c24baaa
- Tests: ./gradlew test --tests '*OffsetAnalyzerTest*' --tests '*StatisticalWindowTest*' --tests '*ConfigManagerTest*' --tests '*FunctionalTest*', ./gradlew checkstyleTest, ./gradlew test
- PRs: