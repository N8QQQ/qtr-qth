---
satisfies: [R5, R6]
---
# fn-2-standardize-legacy-tests-on-given-when.5 Activate Checkstyle MatchXPath gate, execute full test verification, and validate local CI

## Description
Activate the automated Checkstyle `MatchXPath` gate to enforce that AssertJ assertions (`assertThat`) are exclusively called inside `*Fixture` classes, run full test regression suites, and validate all local containerized CI quality gates.

**Size:** S
**Files:** config/checkstyle/checkstyle.xml
**Touches:** [config/checkstyle/checkstyle.xml]

### Approach
- In `config/checkstyle/checkstyle.xml`, add the `MatchXPath` module under `TreeWalker`:
  ```xml
  <module name="MatchXPath">
      <property name="id" value="AssertOnlyInFixture"/>
      <property name="query" value="//METHOD_CALL[.//IDENT[@text='assertThat']][not(ancestor::CLASS_DEF[contains(./IDENT/@text, 'Fixture')])]"/>
      <property name="message" value="assertThat() is restricted to *Fixture classes."/>
  </module>
  ```
- Run `./gradlew checkstyleTest` to verify zero violations across all 15 refactored test suites, `NmeaParserTest`, and reference fixtures.
- Run `./gradlew test jacocoTestReport` to verify 100% test pass rate and validate code coverage.
- Run full `./gradlew check` to verify all linter, typecheck, and formatting gates pass.
- Run local containerized CI suite `./scripts/local-ci.sh --all` (secrets, vuln, lint, codeql).

### Investigation targets
**Required:**
- `config/checkstyle/checkstyle.xml:15-102` — TreeWalker module configuration
- `build.gradle.kts:61-70` — test and coverage tasks
- `scripts/local-ci.sh` — local CI runner script

### Key context
Because all test suites were refactored in Wave 2 (tasks fn-2.2, fn-2.3, fn-2.4), activating this Checkstyle gate now ensures complete verification without intermediate build breakage.

## Acceptance
- [ ] `MatchXPath` assertion boundary module is added to `config/checkstyle/checkstyle.xml`.
- [ ] `./gradlew checkstyleTest` passes with zero violations across all test suites.
- [ ] `./gradlew test` passes 100% of test suites with zero flaky timeouts.
- [ ] `./gradlew check` succeeds cleanly.
- [ ] `./scripts/local-ci.sh --all` completes successfully.

## Done summary
TBD

## Evidence
- Commits:
- Tests:
- PRs:
