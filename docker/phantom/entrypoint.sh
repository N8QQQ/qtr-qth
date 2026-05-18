#!/bin/bash
set -e

echo "--- 🛰️ Phantom Shack: Initializing Virtual Hardware ---"

# 1. Create the Virtual Serial Port
# Mode A: Static Simulation (from file)
# Mode B: Live Coordinator (listens on TCP port 9999)
echo "--- 🛰️ Starting Virtual Hardware Bridge ---"
socat -d -d PTY,link=/dev/ttyUSB99,raw,echo=0 TCP-LISTEN:9999,reuseaddr,fork &
SOCAT_TCP_PID=$!
echo "✅ TCP Coordinator listening on port 9999 -> /dev/ttyUSB99"

# Optional: Seed with initial telemetry if requested
if [ "$SIM_MODE" = "true" ]; then
    echo "📡 Seeding with static telemetry: gps_sim.nmea (Looping)"
    # Continuous stream using a subshell to keep the PTY fed
    (while true; do cat /workspace/src/main/resources/simulation/gps_sim.nmea; sleep 1; done) | socat -  /dev/ttyUSB99,raw,echo=0 &
fi

# 2. Hardware Audit
echo "--- 🔍 Hardware Audit: Enumerating Serial Devices ---"
ls -l /dev/tty* | grep "USB" || echo "⚠️ No physical USB/Serial devices detected."

# 3. Execution
# We run a build and then execute the hardware discovery probe
./gradlew --no-daemon build
./gradlew --no-daemon probeHardware

echo "--- 🚀 Launching Phantom Shack ---"
# For now, we tail the log to keep the container alive and the TTY open
tail -f /dev/null
