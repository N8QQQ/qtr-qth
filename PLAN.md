# Project Plan: qtr-qth (GPS Time & Location Sync)

## Objective
Develop a cross-platform Java application (`qtr-qth`) that reads NMEA 0183 data from a GPS USB dongle to synchronize the system clock (QTR) and provide high-precision location data (QTH) for Amateur Radio operations. The app will also compare GPS time with local LAN/WWW NTP servers and act as a local hub by broadcasting location data to other Ham Radio software.

## Key Files & Context
- **Project Name:** `qtr-qth`
- **Technology Stack:** Java 17+, Gradle, jSerialComm, Apache Commons Net.
- **Target Platforms:** Windows (COM ports), Linux/macOS (/dev/tty*).

## Implementation Steps

### Phase 1: Project Restructuring & Branding (COMPLETED)
1.  **Folder Structure:** Flatten the nested `C:\src\gps-time\gps-time` directory.
2.  **Gradle Config:** Updated `settings.gradle.kts` and `build.gradle.kts` to reflect the new project name `qtr-qth`.
3.  **Repo Setup:** Initialized Git and pushed to GitHub (`origin`) and local bare backup (`local-backup`).
4.  **Dependencies:** Added `jSerialComm` and `Apache Commons Net`.

### Phase 2: Serial Communication & NMEA Parsing (BRANCH: feat/nmea-nibbles)
1.  **Configuration Bootstrapping**:
    *   Implement `ConfigManager` to handle `qtr-qth.properties`.
    *   Support basic settings: Port, Baud, NTP Server, and Sync Thresholds.
2.  **Serial Port Discovery & Connection**:
    *   Utility to list all available hardware ports.
    *   Implement "Smart Connect" logic to open the configured port with standard GPS settings (9600 baud, 8N1).
3.  **The "NMEA Nibbler" (Data Ingestion)**:
    *   Implement an asynchronous `SerialPortDataListener`.
    *   Use a `StringBuilder` to accumulate raw bytes into complete NMEA sentences.
4.  **Parsing Engine (Basic)**:
    *   Extract UTC time and date from `$GPRMC` or `$GPZDA`.
    *   Extract Latitude, Longitude, and Altitude from `$GPGGA`.
    *   Checksum validation for each sentence.
5.  **Grid Square Calculation**:
    *   Convert Latitude/Longitude to 6-character Maidenhead Grid Square.

### Phase 3: Network & Timing Logic
1.  **NTP Client:** Implement `NTPUDPClient` to fetch time from `pool.ntp.org`.
2.  **Clock Comparison:** Calculate the offset between:
    *   System Clock vs. GPS Time.
    *   System Clock vs. Network Time.
    *   GPS Time vs. Network Time (The "Golden Record").

### Phase 4: System Clock Synchronization (The "Actuator")
1.  **OS Command Execution:**
    *   **Windows:** Use `cmd /c time HH:MM:SS` (requires Elevation).
    *   **Linux/Unix:** Use `sudo date -s ...` or `clock_settime`.
2.  **Safety Mechanism:** Implement a "Maximum Drift Threshold" to prevent accidental massive time jumps.

### Phase 5: The "QTH Hub" (Location Broadcasting)
1.  **NMEA Forwarding:** Broadcast parsed NMEA sentences over local UDP so other Ham apps (like WSJT-X or N1MM) can read the exact location/time without fighting over the COM port.
2.  *(Optional)* **GPSd Emulation:** Implement a basic TCP listener on port 2947 to mimic the standard Linux `gpsd` daemon for maximum compatibility with third-party software.

### Phase 6: UI & Dashboard
1.  **Dashboard View:** Display active satellite count, current Grid Square, UTC time, drift metrics, and UDP broadcasting status.
2.  **Logging:** Maintain a rolling log of sync events and drift history.

## Verification & Testing
1.  **Unit Tests:** For NMEA parsing logic and Grid Square calculations.
2.  **Integration Tests:** Mock serial data to simulate GPS input.
3.  **Cross-Platform Check:** Verify serial port detection on both a Windows environment and a Linux (WSL or VM) environment.
