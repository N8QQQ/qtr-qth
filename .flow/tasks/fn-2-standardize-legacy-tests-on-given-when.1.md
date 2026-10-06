---
satisfies: [R1]
---
# fn-2-standardize-legacy-tests-on-given-when.1 Prune scratch tests and update developer testing docs

## Description
Remove obsolete scratch mock tests and document the repository Given-When-Then BDD Fixture standard in developer documentation.

**Size:** S
**Files:** docs/DEVELOPER.md, src/test/java/com/stoicprogrammer/qtrqth/serial/MockitoCheckTest.java, src/test/java/com/stoicprogrammer/qtrqth/serial/SimpleMockTest.java
**Touches:** [docs/DEVELOPER.md, src/test/java/com/stoicprogrammer/qtrqth/serial/MockitoCheckTest.java, src/test/java/com/stoicprogrammer/qtrqth/serial/SimpleMockTest.java]

### Approach
- Delete obsolete scratch files `src/test/java/com/stoicprogrammer/qtrqth/serial/MockitoCheckTest.java` and `src/test/java/com/stoicprogrammer/qtrqth/serial/SimpleMockTest.java`.
- Document the Given-When-Then Fixture Model pattern and assertion boundary constraints in `docs/DEVELOPER.md`.
- Verify with `./gradlew test`.

### Investigation targets
**Required:**
- `docs/DEVELOPER.md:23-38` — Behavior-Driven Testing section to update
- `src/test/java/com/stoicprogrammer/qtrqth/serial/MockitoCheckTest.java` — scratch test to delete
- `src/test/java/com/stoicprogrammer/qtrqth/serial/SimpleMockTest.java` — scratch test to delete

### Key context
The Checkstyle assertion boundary gate will be activated in Wave 3 (task fn-2.5) after all test suites in Wave 2 are fully converted, keeping the build green throughout the migration.

## Acceptance
- [ ] Scratch test files `MockitoCheckTest.java` and `SimpleMockTest.java` are deleted.
- [ ] `docs/DEVELOPER.md` documents the Given-When-Then Fixture pattern and assertion boundary constraints.
- [ ] `./gradlew test` passes.

## Done summary
TBD

## Evidence
- Commits:
- Tests:
- PRs:
