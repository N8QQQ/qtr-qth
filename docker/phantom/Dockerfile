# Phantom Shack: Resource-Constrained Virtual Pi
# High-fidelity development mirror (Debian-slim)

FROM eclipse-temurin:21-jdk-bookworm-slim

# 1. Install System Dependencies (socat for hardware spoofing)
RUN apt-get update && apt-get install -y \
    socat \
    usbutils \
    minicom \
    iproute2 \
    curl \
    git \
    && rm -rf /var/lib/apt/lists/*

# 2. Set Workspace
WORKDIR /workspace

# 3. Validation
# Default command boots a virtual TTY and prepares for build
# We use a background socat to pipe our test NMEA file into a virtual serial device
CMD socat -d -d PTY,link=/dev/ttyUSB99,raw,echo=0 FILE:/workspace/src/main/resources/simulation/gps_sim.nmea & \
    ./gradlew --version && \
    ./gradlew build && \
    echo "--- 🛰️ Phantom Shack Certified: Virtual Hardware Online ---" && \
    tail -f /dev/null
