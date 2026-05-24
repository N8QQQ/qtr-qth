# Hardware Manifest: qtr-qth

This document tracks the physical instrumentation used to develop and certify the `qtr-qth` telemetry hub.

## 🧪 Phase 1: Baseline Laboratory Hardware
The initial development and "Phase 9 Reactive Refactor" were certified using this baseline hardware.

### VFAN UG-353 (G-Mouse Receiver)
- **Status:** In-Service (Reference Unit)
- **Core Engine:** u-blox 7 (UBX-G7020KT)
- **Constellations:** GPS, GLONASS, QZSS, SBAS (WAAS/EGNOS)
- **Protocol:** NMEA 0183 v2.3 (VCP Serial over USB)
- **Sensitivity:** -162 dBm (Tracking)
- **Channels:** 56-Channel Parallel
- **Physical:** IPX6 Water-Resistant, Magnetic Base
- **Role:** Providing the baseline NMEA stream for checksum validation, initial grid-square logic, and Reactive state-flow certification.

---

## 🛰️ Phase 2: High-Fidelity "Heritage" Upgrade (Planned)
The "Ultimate" hardware target for `v1.0.0` and the Phase 10 Offset Engine.

### 🏛️ Laboratory BOM (Desktop/Shack Setup)
| Component | Item | Role |
| :--- | :--- | :--- |
| **Board** | [7SEMI NEO-M9N Breakout](https://www.amazon.com/7SEMI-NEO-M9N-Breakout-Board-Multi-Constellation/dp/B0F9PS33N5) | Multi-GNSS Ingestion Authority |
| **Antenna** | [Beitian L1/L5 Active Antenna](https://www.amazon.com/Beitian-Multi-Band-Precision-ANN-MB-00-BT-3B45AJL5/dp/B0D78H3GRS) | High-Gain L1/L5 Signal Capture |
| **Strain Relief** | [SMA Female to SMA Male Bulkhead (6")](https://www.amazon.com/s?k=SMA+female+to+male+bulkhead+pigtail+RG316) | Mechanical PCB protection |
| **Ground Plane** | 12" x 12" Steel Sheet | Passive Signal Amplification |

### 🏠 Outdoor Authority BOM (Permanent Shack)
| Component | Item | Role |
| :--- | :--- | :--- |
| **Arrestor** | [SMA DC-Pass Lightning Arrestor](https://www.amazon.com/s?k=SMA+lightning+arrestor+DC-Pass) | Lightning/Static Surge Protection |
| **Cable (30ft)** | [SMA Male to SMA Male LMR-240](https://www.amazon.com/s?k=SMA+male+to+male+LMR-240+30ft) | Low-Loss Indoor/Outdoor Feedline |
| **Grounding** | #10 AWG Solid Copper | Single Point Ground (SPG) bonding |
| **Weatherproofing** | Self-Amalgamating Silicone Tape | Connector Sealing (IP67 Enforcement) |

---

## 📐 Enclosure Design Tasks
- [ ] **Task 17.6.A:** Design 3D-printable enclosure for 7SEMI NEO-M9N (56x31mm).
- [ ] **Task 17.6.B:** Implement recessed bulkhead pocket for SMA strain relief.
- [ ] **Task 17.6.C:** Verify thermal ventilation for high-gain active LNA operations.
