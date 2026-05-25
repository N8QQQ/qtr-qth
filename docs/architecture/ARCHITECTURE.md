---
layout: default
title: System Architecture
---
# System Architecture: qtr-qth

`qtr-qth` is a high-precision, event-driven telemetry hub. The architecture prioritizes functional purity, thread-safety, and high-fidelity observability through a strictly decoupled Hardware Abstraction Layer (HAL).

## 🏛️ Component Blueprint (Structural View)
The system is organized into decoupled functional blocks. The `SystemOrchestrator` serves as the primary lifecycle manager, coordinating the interaction between data providers and the telemetry pipeline.

```mermaid
classDiagram
    direction TB
    
    class SystemOrchestrator {
        -ConfigManager configManager
        -InstantSource clock
        -ScheduledExecutorService ntpExecutor
        -AtomicReference~NtpResponse~ lastNtp
        -AtomicReference~GpsData~ currentFix
        -SerialConnector connector
        +start(Consumer~TelemetryPulse~)
        +shutdown()
    }

    class SerialConnector {
        -ISerialProvider provider
        -NmeaSentenceAccumulator accumulator
        -ISerialPort activePort
        -LinkedBlockingQueue~String~ queue
        +connect(String portName) Stream~String~
        +disconnect()
    }

    class NtpClient {
        -INtpProvider provider
        +pollDetailed(List~String~ hosts) Optional~NtpResponse~
    }

    class TelemetryPulse {
        <<record>>
        +String pulseId
        +String triggeringSentence
        +Instant ingressTime
        +GpsData data
        +NtpResponse reference
        +ConfluenceHealth health
    }

    class NmeaParser {
        +parse(String, GpsData) GpsData
        +isTrigger(String) boolean
    }

    SystemOrchestrator *-- SerialConnector : owns
    SystemOrchestrator *-- NtpClient : delegates to
    SystemOrchestrator ..> TelemetryPulse : produces
    SerialConnector o-- ISerialProvider : utilizes
    SerialConnector *-- NmeaSentenceAccumulator : owns
```

## 📐 Package Hierarchy & Dependency Graph (Structural Purity)
The system enforces a strict **Top-Down Unidirectional Flow**. My audit of the `v0.6.0` codebase certifies **Zero Circular Dependencies**.

```mermaid
graph TD
    subgraph "1. Lifecycle Layer (Boot)"
        MAIN[com.stoicprogrammer.qtrqth.Main] --> ORCH[SystemOrchestrator]
    end

    subgraph "2. Orchestration Layer (Confluence)"
        ORCH --> CONF[config]
        ORCH --> SER[serial]
        ORCH --> NTP[ntp]
        ORCH --> MOD[model]
        ORCH --> NMEA[nmea]
    end

    subgraph "3. Plumbing Layer (HAL)"
        SER --> SER_API[serial.api]
        SER --> SER_SIM[serial.simulation]
        SER --> SER_JSC[serial.jserialcomm]
        NTP --> NTP_API[ntp.api]
        NTP --> NTP_NET[ntp.network]
        NTP --> NTP_SIM[ntp.simulation]
    end

    subgraph "4. Domain Layer (The Pulse)"
        MOD --> NMEA
        MOD --> NTP
        MOD --> UTIL[util]
    end

    subgraph "5. Foundation (Pure Utilities)"
        NMEA --> UTIL
        CONF --> UTIL
        SER_API
        NTP_API
    end

    style MAIN fill:#f9f,stroke:#333,stroke-width:2px
    style MOD fill:#bbf,stroke:#333,stroke-width:2px
    style UTIL fill:#dfd,stroke:#333,stroke-width:2px
```

### 🛡️ Dependency Mandates:
1.  **Acyclic Flow:** No package in a lower layer may depend on a higher layer.
2.  **Domain Purity:** The `model` and `nmea` packages must remain free of OS-level side effects (I/O, Clocks, Network).
3.  **Monadic Foundation:** The `util` package provides the functional primitives used by all layers for safe parsing and exception handling.

## 🔄 System Lifecycle (Behavioral View)
The application operates as a deterministic state machine. The transition from `BOOTING` to `ACTIVE` involves a "State-Lock" where the operational mode is committed and never changed.

```mermaid
stateDiagram-v2
    [*] --> BOOTING : System Start
    
    state BOOTING {
        direction TB
        INIT_CONFIG --> RESOLVE_MODE
        RESOLVE_MODE --> LOCK_MODE
    }
    
    BOOTING --> ACTIVE : Mode Locked (Hardware Found)
    BOOTING --> SIMULATION : Mode Locked (Fallback/Manual)
    
    state ACTIVE {
        [*] --> DISCOVERY
        DISCOVERY --> STREAMING : Hardware Acquired
        STREAMING --> STREAMING : Reactive Ingress
        
        STREAMING --> RECOVERY : Signal Loss
        RECOVERY --> NEUTRALIZATION : Disconnect Stale Handle
        NEUTRALIZATION --> DISCOVERY : Backoff Wait
        DISCOVERY --> DISCOVERY : Hardware Missing
    }

    state SIMULATION {
        [*] --> VIRTUAL_STREAMING
    }
    
    ACTIVE --> SHUTDOWN : SIGTERM / User Stop
    SIMULATION --> SHUTDOWN : SIGTERM / User Stop
    SHUTDOWN --> [*] : Resources Released
```

## 📡 The "Reactive Confluence" Model (Data Flow)
The core of the system is based on two asynchronous streams of data merging into a unified telemetry pulse. The "Fast River" (GPS) is processed as a deterministic monolith to ensure absolute temporal fidelity.

```mermaid
flowchart TD
    subgraph "The Slow River (Network)"
        NTP["NTP Pool"] --> |Poll 60s| NC["NtpClient"]
        NC --> |NtpResponse| AR["AtomicReference: lastNtp"]
    end

    subgraph "The Fast River (Hardware)"
        GPS["GPS Hardware"] --> |Byte Stream| SC["SerialConnector"]
        SC --> |Edge Stamp: T1| EQ["LinkedBlockingQueue: TelemetryEvent"]
        EQ --> |Poll| RM["Reactive Monolith: Core 1"]
        
        subgraph "Reactive Monolith"
            RM --> |Parse| NP["NmeaParser"]
            RM --> |Update| SR["Reactive State Registry"]
            RM --> |Trigger| TP["TelemetryPulse::start"]
        end
    end

    CLOCK["InstantSource: Ground Truth"] --> |T1 Timestamp| SC
    AR -.-> |Merge| TP
    SR -.-> |Enrich| TP
    TP --> |GpsData| GS["GridSquareCalculator"]
    GS --> |Log| LOG["High-Fidelity Console UI"]
```

1.  **The Slow River (NTP):** A background heartbeat providing a network-based "second opinion."
2.  **The Fast River (GPS):** A deterministic monolith where **Producer-side Edge Stamping** captures the system clock at the instant of byte arrival. 
3.  **Monolithic Processing:** By using a single consumer thread for all GPS sentences, the system eliminates state-tearing and ensures that position data is always fully enriched before a timing pulse is emitted.
4.  **Zero-Jitter Log:** The `nmea.log` uses a `neverBlock` policy to ensure logging performance never impacts the serial ingestion timing.

## 🛡️ Structural Guardrails
- **Typed Configuration:** Immutable `AppConfig` records eliminate "Stringly-Typed" logic.
- **Numeric Purity:** Monadic wrappers (`tryParseInt`) catch hardware noise.
- **State-Lock:** Boot-time determination of HARDWARE vs SIMULATION modes ensures runtime stability.

---
*For historical tactical details, see the [Phase Designs](../design/DESIGN_PHASE_1.md).*
