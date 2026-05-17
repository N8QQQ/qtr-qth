# Technical Design: Phase 3 - Observability (v0.2.0)

## 🎯 Objective
Establish a professional logging infrastructure and implement per-pulse traceability across asynchronous boundaries.

## 🏗️ Architectural Components

### 1. Unified Logging Strategy (SLF4J/Logback)
Dual-stream logging to separate business telemetry ("Shack Log") from internal debugging ("Lab Log").

```mermaid
flowchart TD
    App[Application Logic] --> SLF4J[SLF4J Facade]
    SLF4J --> LB[Logback Engine]
    LB --> Console[Console Appender]
    LB --> File[Rolling File Appender]
    LB --> Async[AsyncAppender: Zero Latency]
```

### 2. Mapped Diagnostic Context (MDC)
Injecting unique **Pulse IDs** into the thread context to ensure logs from different rivers (Fast/Slow) can be correlated.

```mermaid
sequenceDiagram
    participant Main as Telemetry Pipeline
    participant MDC as Logback MDC
    participant Logger as SLF4J Logger

    Main->>MDC: put("pulseId", hash)
    Main->>Logger: info("GPS Fix...")
    Logger-->>Main: [ID: 4F2A] GPS Fix...
    Main->>MDC: clear()
```

## 🧪 Verification Strategy
- **Context Traceability:** Assert that every log entry during a pulse contains the correct 4-character hex ID.
- **Performance:** Verify that async logging does not block the 1Hz hardware ingestion river.
