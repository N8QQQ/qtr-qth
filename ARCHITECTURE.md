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

## 🛠️ Design Patterns

### 1. Hardware Abstraction Layer (HAL)
We utilize a Provider/Wrapper pattern to decouple the core logic from physical hardware. This allows the system to run in **Simulation Mode** with identical process flows to real hardware.

### 2. Pure Functional Parsing
The `NmeaParser` is a stateless pure function. It accepts `(String sentence, GpsData previous)` and returns `GpsData next`. This ensures 100% predictable state transitions and simplifies unit testing.

### 3. Contextual Observability (MDC)
Every pulse is assigned a unique **Trace ID**. We utilize SLF4J's Mapped Diagnostic Context to ensure every log entry generated during a pulse's lifecycle is tagged with this ID, enabling end-to-end traceability across threads.

## 📡 Authority & Stratum
By directly connecting to GPS (Stratum 0), `qtr-qth` effectively operates as a **Stratum 1** authority for the local shack, using NTP as a "Second Opinion" for drift verification.
