# qtr-qth : GPS Time & Location Hub

`qtr-qth` is a cross-platform application for Amateur Radio operators. It synchronizes your system clock (QTR) and provides precise location data (QTH), including Maidenhead Grid Squares, using a standard GPS USB receiver.

## 🚀 Capabilities

- **Time Sync:** Precision UTC time extraction from GPS satellites.
- **Location Hub:** Real-time Latitude, Longitude, and Altitude monitoring.
- **Grid Square:** Automatic 6-character Maidenhead Grid Square calculation (e.g., `FN20xr`).
- **Sim Mode:** Built-in GPS simulator for testing away from the radio shack.

## 🏃 Getting Started

### 1. Requirements
- **Java 21:** Ensure you have Java 21 installed on your system.
- **GPS Device:** (Optional) A USB GPS dongle (u-blox, VFAN, etc.).

### 2. Configuration
The application uses a `qtr-qth.properties` file. It will be created automatically on first run, but you can customize it as needed:

| Property | Default | Description |
| :--- | :--- | :--- |
| `simulation.mode` | `true` | Set to `false` to use real hardware. |
| `display.raw.telemetry` | `false` | Set to `true` to see raw NMEA data from the GPS. |
| `serial.baud` | `9600` | The speed of your GPS device (usually 9600). |
| `gps.discovery.keywords` | `gps,u-blox...` | Keywords to help find your GPS hardware. |

### 3. Running the Application
Download the latest release and run the script for your operating system:

**Windows:**
```powershell
./qtr-qth.bat
```

**Linux / macOS:**
```bash
./qtr-qth
```

### 4. Linux Permissions
If you are running on Linux (including Raspberry Pi), you may need to grant your user permission to access serial ports. We've included a helper script for this:
```bash
chmod +x scripts/linux-setup.sh
./scripts/linux-setup.sh
```
*(A logout/relogin is required after running this script).*

## 🔍 Troubleshooting Hardware
If your GPS device is not detected:
1. Set `display.raw.telemetry=true` in the properties file.
2. Run the application and check the `[SERIAL]` logs.
3. If you see your device name in the logs but it isn't "Auto-Found", add a keyword from that name to `gps.discovery.keywords`.

## ⚖️ License & Copyright

Developed by **Nicholas R. Ustick (N8QQQ)** - [StoicProgrammer.com](https://StoicProgrammer.com)

Copyright (c) 2026 Nicholas R. Ustick. This project is licensed under the **GNU General Public License v3.0**. See the [LICENSE](LICENSE) file for full details.

## 📝 Scientific Citation

If you use this software for research, technical papers, or formal radio experiment reports, please cite it using the metadata in the [CITATION.cff](CITATION.cff) file. This project is indexed for archival preservation on **Zenodo**.

*Engineered in collaboration with JARVIS (via Gemini CLI).*

---
Technical details for developers can be found in [DEVELOPER.md](DEVELOPER.md).
