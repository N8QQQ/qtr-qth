# Technical Design: Phase 2 - Serial Plumbing (v0.2.0)

## 🎯 Objective
Implement a robust, decoupled hardware abstraction layer for serial communication and establish a multi-threaded data ingestion pipeline.

## 🏗️ Architectural Components

### 1. Serial HAL (`com.stoicprogrammer.qtrqth.serial.api`)
A Hardware Abstraction Layer that decouples the application from physical JNI dependencies.

```mermaid
classDiagram
    class ISerialProvider {
        <<interface>>
        +getAvailablePorts() List~ISerialPort~
        +getPort(String name) ISerialPort
    }
    class ISerialPort {
        <<interface>>
        +openPort() boolean
        +closePort() boolean
        +readBytes(byte[] buf, int len) int
        +addDataListener(SerialPortDataListener l) boolean
    }
    ISerialProvider ..> ISerialPort : creates
    class JSerialCommProvider {
        +getAvailablePorts()
    }
    class SimulationSerialProvider {
        +getAvailablePorts()
    }
    ISerialProvider <|-- JSerialCommProvider
    ISerialProvider <|-- SimulationSerialProvider
```

### 2. The Ingestion Pipeline
Asynchronous byte-to-sentence accumulation to prevent UI/Pipeline blocking.

```mermaid
sequenceDiagram
    participant HW as Serial Hardware
    participant SC as SerialConnector
    participant AC as NmeaSentenceAccumulator
    participant Q as LinkedBlockingQueue

    HW->>SC: Data Available Event
    SC->>HW: readBytes()
    loop For each byte
        SC->>AC: process(byte)
        AC-->>SC: Optional~Sentence~
        Note over SC,AC: If present, offer to queue
        SC->>Q: offer(sentence)
    end
```

### 3. Port Auto-Discovery
Heuristic-based scanning to identify likely GPS hardware based on manufacturer metadata (u-blox, Prolific, etc.).

## 🧪 Verification Strategy
- **BDD Integration:** Verify that simulated hardware generates valid NMEA streams.
- **Resilience:** Assert that malformed bytes are handled by the accumulator without losing subsequent sentence synchronization.
