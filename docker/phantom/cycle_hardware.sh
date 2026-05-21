#!/bin/bash
# High-fidelity Hardware Life-Cycle Simulator
# Cycles a virtual TTY to test self-healing logic.

VTTY="/dev/ttyUSB99"
SOURCE="/workspace/src/main/resources/simulation/gps_sim.nmea"

while true; do
    echo "--- 🛰️ Simulating GPS CONNECTION ---"
    # Create the PTY and keep it open for 30 seconds
    socat -d -d PTY,link=$VTTY,raw,echo=0 FILE:$SOURCE,loop=1 &
    SOCAT_PID=$!
    sleep 30
    
    echo "--- ❌ Simulating GPS DISCONNECTION ---"
    kill $SOCAT_PID
    sleep 15
done
