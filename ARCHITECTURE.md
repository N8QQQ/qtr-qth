# System Architecture: qtr-qth

`qtr-qth` is designed as a high-precision, event-driven telemetry hub. The architecture prioritizes functional purity, thread-safety, and observability.

## 🏛️ The "Two-River Confluence" Model

The system operates using two asynchronous "Rivers" of data that merge without blocking, ensuring that network latency never compromises hardware precision.

### 1. The GPS River (High-Frequency)
- **Source:** GPS Satellites (Stratum 0).
- **Ingestion:** Driven by hardware interrupts via `jSerialComm`.
- **Thread:** Dedicated Serial Event thread.
- **Pacing:** ~1 Hz (1 pulse per second).
- **Paradigm:** Pure Functional. Raw bytes are accumulated into sentences, then parsed into immutable state.

### 2. The NTP River (Low-Frequency)
- **Source:** Network Atomic Clocks (Stratum 1/2).
- **Ingestion:** Polled via UDP using `Apache Commons Net`.
- **Thread:** Dedicated `ScheduledExecutorService` (The "Heartbeat" thread).
- **Pacing:** Configurable (Default: 60 seconds).

### 3. The Confluence (Lock-Free Merge)
The two rivers merge at the **Telemetry Pipeline** using the **Atomic Snapshot** pattern.
- The latest NTP poll is stored in an `AtomicReference<NtpResponse>`.
- The GPS River takes a non-blocking snapshot of this reference during every pulse.
- **Result:** A single, immutable `TelemetryPulse` packet containing both GPS reality and Network reference.

## 🛠️ Engineering Tenets

The system is built on four core pillars to ensure long-term viability and "Heritage Grade" quality:

1.  **Testability (TDD/BDD):** No functional logic exists without a corresponding automated test. The system is decoupled via HAL and Interfaces to ensure 100% test coverage of business rules.
2.  **Extensibility:** The "Two-River Confluence" model allows for adding new data sources (e.g., secondary GPS, Weather telemetry) by simply adding a new "River" thread and an Atomic Reference.
3.  **Refactorability:** Using pure functions and immutable records makes logic easy to move, modify, and optimize without side-effects.
4.  **Third-Party Usability:** Core components (`NtpClient`, `NmeaParser`, `GridSquareCalculator`) are designed as high-quality, decoupled modules that can be easily used by third parties or in forked projects.

## 🛠️ Design Patterns

### 1. Hardware Abstraction Layer (HAL)
We utilize a Provider/Wrapper pattern to decouple the core logic from physical hardware. This allows the system to run in **Simulation Mode** with identical process flows to real hardware.

### 2. Pure Functional Parsing
The `NmeaParser` is a stateless pure function. It accepts `(String sentence, GpsData previous)` and returns `GpsData next`. This ensures 100% predictable state transitions and simplifies unit testing.

### 3. Contextual Observability (MDC)
Every pulse is assigned a unique **Trace ID**. We utilize SLF4J's Mapped Diagnostic Context to ensure every log entry generated during a pulse's lifecycle is tagged with this ID, enabling end-to-end traceability across threads.

## 📡 Authority & Stratum
By directly connecting to GPS (Stratum 0), `qtr-qth` effectively operates as a **Stratum 1** authority for the local shack, using NTP as a "Second Opinion" for drift verification.

## 📉 Stability & Drift Analysis (Phase 5)
To certify the precision of the system clock, the system implements a secondary analytical pipeline:

1.  **Differential Calculus:** The system calculates the high-precision delta between `Local Clock` and `Reference Clock` (GPS/NTP) at the exact moment of pulse arrival.
2.  **Statistical Smoothing:** Real-time offsets are stored in a **Sliding Window Buffer**, where we calculate the arithmetic mean and standard deviation (Jitter).
3.  **Heuristic Scoring:** A weighted heuristic is applied to the metadata (Stratum, RTT, Fix Quality) to assign a **Stability Grade** to the shack's time health.
