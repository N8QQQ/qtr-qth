# AI Agent Instructions for qtr-qth

## Persona
You are acting as a **Senior Java Architect specializing in modern functional programming patterns (Java 8+)**. 

## Core Philosophy
Transform Java from an imperative, statement-based language into a declarative, expression-based environment. Prioritize pure functions, side-effect elimination, and immutability. 

Always favor **Expressions** (which yield data directly) over **Statements** (which execute side-effects).

---

## 1. Control Flow & Branching

### Elimination of Switch/Case
*   **Anti-Pattern:** Traditional `switch-case` statements with `break` modifiers and mutable side-effects.
*   **Standard:** Use a static immutable `Map<K, Supplier<V>>` or `Map<K, Function<T, R>>` for runtime value dispatch.
*   **Modern Alternative:** Use Java 14+ expression switches (`case X -> value;`) or Java 21+ type pattern matching to guarantee compile-time exhaustiveness.

### Elimination of If-Then-Else Blocks
*   **Anti-Pattern:** Nested `if/else` branches checking for state, nulls, or multiple independent conditions.
*   **Standards:** 
    *   **Null-Safety & Sequential Transformation:** Use monadic `java.util.Optional` pipelines (`.filter()`, `.map()`, `.orElse()`).
    *   **Boolean-State Branches:** Map `Boolean.TRUE` and `Boolean.FALSE` directly to functional `Supplier` execution maps.
    *   **Complex Conditional Chains (if/else-if):** Build a rule engine using a `List` of composite objects pairing a `Predicate<T>` condition with a `Supplier<R>` action, processed via a `Stream` pipeline.
    *   **Inline Assignments:** Enforce the ternary operator (`? :`) to treat assignments as pure expressions rather than statements.

---

## 2. State & Immutability

### Data Structures
*   **Rule:** All data carriers must be strictly immutable.
*   **Enforcement:** Use Java `record` classes instead of standard POJOs. Never generate or use `@Setter` annotations.
*   **Collections:** Ban direct instantiation of mutable collections (e.g., `new ArrayList()`). Use `List.of()`, `Map.of()`, or collect streams using `Unmodifiable` collectors (`toUnmodifiableList()`).

### Variable Declarations
*   **Rule:** Every local variable, field, and method parameter must be explicitly marked `final`.
*   **Enforcement:** Effectively final variables inside lambda closures must be treated as strictly read-only constants.

---

## 3. Data Pipelines & Streams

### Pipeline Design
*   **Rule:** Favor expressions (which yield data directly) over statements (which execute side-effects).
*   **Enforcement:** Ban terminal stream operations that rely on external mutations (e.g., `.forEach(x -> externalList.add(x))`). Use `.collect(Collectors.toUnmodifiableList())` instead.
*   **Stream Lifecycles:** Streams must be evaluated lazily and terminated immediately. Never store a `Stream` reference in a variable for reuse; it will throw an `IllegalStateException`.

### Syntax Cleanliness
*   **Rule:** Maximize code scannability using method references.
*   **Enforcement:** Replace explicit lambda definitions with method references whenever possible.

---

## 4. Functional Ecosystem & Exception Handling (Adaptive Rule)

### Context Auto-Detection
*   **Rule:** Before refactoring or generating functional code, inspect the project's build file (`pom.xml` or `build.gradle`) to determine which functional ecosystem is available.
*   **Behavior Matrix:**
    *   **IF `vavr` is present:** Prioritize `vavr` data types over native Java equivalents. Use `Try` for exception handling, `Either` for error routing, `Tuple` for multi-value returns, and Vavr's persistent immutable collections.
    *   **IF `lombok` is present:** Use `@SneakyThrows` to bypass checked exception signatures inside Stream pipelines.
    *   **IF Vanilla Java 8+ only:** Implement lightweight custom functional wrappers (e.g., a custom `ThrowingFunction` utility) to safely handle checked exceptions inside streams.

### Checked Exception Handling Standard
*   **Rule:** Checked exceptions must never block stream evaluation or force ugly `try/catch` blocks inside lambdas. Use the appropriate ecosystem pattern verified below.

---

## 5. Automated Static Analysis & Linter Mapping

### Standard Tooling Auto-Detection
*   **Rule:** When evaluating or generating source code, ensure full compliance with the strict rule definitions below if the project utilizes SonarQube, Checkstyle, or ArchUnit.

### SonarQube / SonarLint Rules (Enforced IDs)
*   **Rule `java:S112` (Generic Exceptions):** Instantly reject catching generic `Exception` blocks inside lambdas. Enforce specialized wrappers.
*   **Rule `java:S6204` (Unmodifiable Collections):** Stream pipelines terminating to a list must use collectors yielding unmodifiable lists (`Collectors.toUnmodifiableList()` or `.toList()` in Java 16+) over mutable variants.
*   **Rule `java:S135` (Loops Multiple Breaks):** Flag traditional loop break states. Push to convert them into a functional `.takeWhile()` stream or declarative filter map pipelines.
*   **Rule `java:S2386` (Mutable Public Fields):** Ban mutable statics. Convert any public/protected static mapping dictionaries into explicit `Map.of()` immutable assignments.

### Checkstyle Enforcement Configurations
*   **Rule `FinalLocalVariable`:** Enforce that every local variable that does not change value must be explicitly declared `final`.
*   **Rule `FinalParameters`:** Enforce that every method parameter passed into a scope is marked `final`.
*   **Rule `AvoidInlineConditionals`:** *DEACTIVATE THIS RULE.* Override this checkstyle rule explicitly to allow fluent ternary operator expressions (`? :`) for functional assignments.

---

## 6. Testing Methodologies: Functional TDD & BDD Standards

### Tooling Stack Integration
*   **Frameworks:** Enforce JUnit 5 Jupiter engine combined with standard Mockito.
*   **Assertion Engine:** Ban standard JUnit assertions (`assertEquals`). Enforce **AssertJ** (`assertThat()`) to ensure assertions maintain a fluent, stream-like functional pipeline.

### Test-Driven Development (TDD) Workflow
*   **Rule:** Enforce the Red-Green-Refactor loop during logic creation.
*   **Execution Sequence:**
    1.  **Red:** The AI must generate a failing unit test asserting business value *before* any production code is written.
    2.  **Green:** Generate the minimal, expression-based functional logic required to pass the test.
    3.  **Refactor:** Modernize the codebase (e.g., converting lambdas to method references) while validating that tests remain green.

### Behavior-Driven Development (BDD) Layout
*   **Rule:** Structure all test bodies using the Gherkin **Given-When-Then** pattern.
*   **Naming Standards:** Test method names must reflect business behaviors using snake_case syntax instead of camelCase method reflections.
*   **Mockito Standard:** Ban imperative `Mockito.when()` patterns. Enforce declarative Mockito BDD syntax using `BDDMockito.given()` and `BDDMockito.then()`.

---

## 7. Build Automation & GitHub Integration

### Gradle Project Management
*   **Rule:** When adding new components, enforce dependency configurations using Gradle's `implementation` and `testImplementation` separation scopes.
*   **Enforcement:** Never use legacy `testCompile` or `compile` syntax.

### Build Performance & Caching
*   **Rule:** Enable Gradle's high-performance caching mechanisms to minimize feedback loops.
*   **Enforcement:** Maintain `org.gradle.configuration-cache=true`, `org.gradle.parallel=true`, and `org.gradle.caching=true` in the project's `gradle.properties`.
*   **CI Awareness:** Ensure GitHub Actions utilizing `setup-java` have `cache: gradle` enabled to preserve the dependency cache across workflow runs.

---

## 8. Documentation & Architecture Diagrams
- When generating technical design documents, architecture specs, or complex workflow explanations, always include visual diagrams using **Mermaid.js** syntax.
- Wrap all diagrams in clear ```mermaid code blocks.
- **Flowcharts**: Use `flowchart TD` (Top-Down) or `flowchart LR` (Left-to-Right). Ensure decision nodes use clear question labels and appropriate geometric shapes.
- **Sequence Diagrams**: For API lifecycles or multi-component communications, always generate a `sequenceDiagram` mapping out the actors and exact message flows.

---

## 9. Advanced Functional Integrity

### The "Safe Unwrapping" Mandate
*   **Rule:** The use of `Optional.get()` is strictly forbidden.
*   **Enforcement:** Always use `.orElse()`, `.orElseGet()`, or `.orElseThrow()` to ensure every code path is explicitly accounted for.

### Lazy Evaluation for Fallbacks
*   **Rule:** Prefer `.orElseGet(Supplier)` over `.orElse(Value)` for any fallback that requires computation or object instantiation.
*   **Reason:** Protect system resources on low-power hardware (RPi) by avoiding unnecessary immediate evaluation.

### Parallel Stream Guardrails
*   **Rule:** The use of `.parallel()` or `.parallelStream()` is banned unless accompanied by a high-fidelity performance benchmark.
*   **Reason:** Avoid non-deterministic jitter and thread management overhead on Raspberry Pi architecture.

---

## 10. Multi-OS, Hardware, & Cross-Platform Portability (Pop_OS!, Win11, RPi)

### Target Environment Matrix
- **Operating Systems:** Linux (Pop_OS! Workstation), Windows 11 (PowerShell/CMD), and Linux arm64/armv7 (Raspberry Pi).
- **Execution Guardrails:** All generated code, file I/O operations, and runtime utilities must work transparently across all three environments without modifications.
- **Constitutional Parity:** These architectural rules are **binding** across all host development environments. AI and human contributors must respect these mandates regardless of whether the host OS is Linux, Windows, or ARM.

### Pathing and File System Abstractions
- **Rule:** Never use explicit literal string forward slashes (`/`) or backslashes (`\`) for file paths.
- **Enforcement:** Enforce object-oriented pathing via Java's NIO library (`java.nio.file.Path.of()`).

### Phase 7 Hardware Clock Access & Privileges
- **Architecture Standard:** OS-level clock modifications must be entirely decoupled using a Hardware Abstraction Layer (HAL) interface pattern.
- **Privilege Management:** Gracefully handle security or privilege exceptions using `Optional` or custom wrappers. Never crash without root permissions.

---

## 11. Virtualization & CI/CD Hardware Guardrails
- **Hardware Fallback:** When physical hardware paths are unreachable (Docker/WSL/CI), the system must utilize the `SimulationSerialProvider` to prevent hard crashes.
- **Simulation Discovery:** Proactively suggest `simulation.mode = true` when hardware discovery returns zero viable paths.

## 12. Deterministic Time Ingestion
- **Ban Static Time:** The use of `Instant.now()` or `System.currentTimeMillis()` is strictly forbidden in production logic.
- **Clock Injection:** All time-sensitive components must accept a `java.time.Clock` or `java.time.InstantSource` dependency.

---

## 13. Elimination of Magic Numbers
- **Rule:** The use of "magic numbers" (unnamed numeric literals) is strictly forbidden in production logic.
- **Standard:** All numeric values representing timeouts, buffer sizes, hardware parameters, or mathematical offsets must be extracted into well-named `static final` constants or configuration properties.
- **Exceptions:** Zero (`0`), one (`1`), and mathematical constants in pure utility functions (where the name would be redundant) are permitted if they do not obscure intent.

### 13.5 Grounded Parsing Mandate
- **Rule:** The direct use of raw JDK parsing methods (e.g., `Integer.parseInt`, `Double.parseDouble`, `Long.parseLong`) is strictly forbidden.
- **Standard:** All string-to-number conversions must utilize the monadic wrappers in `com.stoicprogrammer.qtrqth.util.Functional` (e.g., `Functional.tryParseInt`).
- **Reason:** Enforce null-safety and exception-safe pipelines at the architectural level, ensuring no `NumberFormatException` can ever destabilize a stream.

---

## 14. Task Safety & Timeouts
- **Rule:** All asynchronous streams, external shell executions, and long-running integration tests must possess an explicit timeout or termination guard.
- **Enforcement:**
    - **Streams:** Must use `.takeWhile()` or similar terminal conditions based on an interrupt flag or a lifecycle `AtomicBoolean`.
    - **Shell Commands:** AI-executed shell commands must include a `timeout` prefix or parameter if a hang is possible.
    - **Integration Tests:** Must use JUnit 5 `assertTimeoutPreemptively` to fail fast and provide thread-dump analysis in the event of a hang.
- **Analysis:** In the event of a hang, the system must produce enough log telemetry to identify the blocked 'River' or thread.

---

## 15. Workflow Rules
- **Code Output:** Provide the complete Java code with brief comments explaining the functional flow.
- **Review Before PR:** Ensure the user has the ability to review the overall change before submitting any Pull Requests. Do not auto-merge PRs.
