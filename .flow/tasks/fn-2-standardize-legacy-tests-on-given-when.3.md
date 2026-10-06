---
satisfies: [R2, R4]
---
# fn-2-standardize-legacy-tests-on-given-when.3 Refactor NTP, Sentinel, and Reactive unit tests with CountDownLatch fixtures

## Description
Standardize network NTP, executor sentinel, reactive stream unit tests, and NmeaParserTest onto the Given-When-Then Fixture pattern, replacing any arbitrary thread sleeps with bounded `CountDownLatch` coordination (3-second timeout).

**Size:** M
**Files:** src/test/java/com/stoicprogrammer/qtrqth/ntp/NtpClientTest.java, src/test/java/com/stoicprogrammer/qtrqth/ntp/simulation/SimulationNtpProviderTest.java, src/test/java/com/stoicprogrammer/qtrqth/sentinel/ExecutorSentinelTest.java, src/test/java/com/stoicprogrammer/qtrqth/nmea/ReactiveInversionTest.java, src/test/java/com/stoicprogrammer/qtrqth/nmea/NmeaParserTest.java
**Touches:** [src/test/java/com/stoicprogrammer/qtrqth/ntp/NtpClientTest.java, src/test/java/com/stoicprogrammer/qtrqth/ntp/simulation/SimulationNtpProviderTest.java, src/test/java/com/stoicprogrammer/qtrqth/sentinel/ExecutorSentinelTest.java, src/test/java/com/stoicprogrammer/qtrqth/nmea/ReactiveInversionTest.java, src/test/java/com/stoicprogrammer/qtrqth/nmea/NmeaParserTest.java]

### Approach
- In `NtpClientTest`, create inner `NtpFixture` encapsulating mock NTP servers, timeout handling, and offset assertions.
- In `SimulationNtpProviderTest`, create inner `SimulationFixture` encapsulating provider drift configurations and assertions.
- In `ExecutorSentinelTest`, create inner `SentinelFixture` utilizing `CountDownLatch` bounded by `3, TimeUnit.SECONDS` to verify task scheduling and execution assertions.
- In `ReactiveInversionTest`, create inner `ReactiveFixture` coordinating asynchronous stream events via `CountDownLatch(3s)` and encapsulating backpressure/inversion assertions.
- In `NmeaParserTest`, encapsulate the 5 loose `assertThat` statements (lines 83, 107, 113-115) into `ParserFixture` so that no loose assertions escape fixture boundaries.
- Verify using `./gradlew test --tests "*NtpClientTest*" --tests "*SimulationNtpProviderTest*" --tests "*ExecutorSentinelTest*" --tests "*ReactiveInversionTest*" --tests "*NmeaParserTest*"`.

### Investigation targets
**Required:**
- `src/test/java/com/stoicprogrammer/qtrqth/nmea/NmeaParserTest.java:80-120` — loose assertions to encapsulate into ParserFixture
- `src/test/java/com/stoicprogrammer/qtrqth/nmea/ReactiveInversionTest.java` — current reactive streaming test assertions
- `src/test/java/com/stoicprogrammer/qtrqth/sentinel/ExecutorSentinelTest.java` — current timer scheduling assertions
- `src/test/java/com/stoicprogrammer/qtrqth/ntp/NtpClientTest.java` — network NTP mock assertions
- `src/test/java/com/stoicprogrammer/qtrqth/ntp/simulation/SimulationNtpProviderTest.java` — simulated NTP assertions

### Key context
Thread.sleep must never be used for timing synchronization. All asynchronous synchronization points must use `assertThat(latch.await(3, TimeUnit.SECONDS)).as("Timed out waiting for %s after 3s", event).isTrue()`.

## Acceptance
- [ ] `NtpClientTest` and `SimulationNtpProviderTest` encapsulate mock state and assertions inside inner `Fixture` classes.
- [ ] `ExecutorSentinelTest` coordinates background execution via `CountDownLatch` with 3-second timeout.
- [ ] `ReactiveInversionTest` encapsulates reactive stream verification in `ReactiveFixture` with 3-second bounded latch waits.
- [ ] `NmeaParserTest` encapsulates all remaining loose `assertThat` calls into `ParserFixture`.
- [ ] All refactored tests pass with `./gradlew test`.

## Done summary
# Task fn-2.3 Summary: Refactor NTP, Sentinel, and Reactive Unit Tests

### Accomplishments
- Refactored `NtpClientTest` to inner `NtpFixture`, encapsulating mock NTP stubs and verification.
- Refactored `SimulationNtpProviderTest` to inner `SimulationFixture`, encapsulating simulated response assertions.
- Refactored `ExecutorSentinelTest` to inner `SentinelFixture`, coordinating background task execution via `CountDownLatch` bounded by 3-second timeout and clean stop lifecycle.
- Refactored `ReactiveInversionTest` to inner `ReactiveFixture` coordinating asynchronous stream emission via `CountDownLatch` with 3-second timeout.
- Encapsulated all 5 remaining loose `assertThat` calls in `NmeaParserTest` (satellite count, null time, trigger evaluation) into `ParserFixture`.
- Zero production code touched.
- Checkstyle and all 69 unit tests pass cleanly.
- Adversarial Carmack implementation review passed with verdict `SHIP`.
## Evidence
- Commits: 36b74ee511cf74092497672ec1d4f2913e6144e5
- Tests: ./gradlew test --tests '*NtpClientTest*' --tests '*SimulationNtpProviderTest*' --tests '*ExecutorSentinelTest*' --tests '*ReactiveInversionTest*' --tests '*NmeaParserTest*', ./gradlew checkstyleTest, ./gradlew test
- PRs: