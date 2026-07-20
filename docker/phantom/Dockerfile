# Phantom Shack: Resource-Constrained Virtual Pi
# High-fidelity development mirror (Ubuntu 24.04 + JDK 21)

FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

# 1. Install System Dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-21-jdk-headless \
    curl \
    git \
    socat \
    usbutils \
    minicom \
    iproute2 \
    && rm -rf /var/lib/apt/lists/*

# 2. Set Workspace
WORKDIR /workspace

# 3. Entrypoint
COPY docker/phantom/entrypoint.sh /usr/local/bin/
COPY docker/phantom/cycle_hardware.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/entrypoint.sh /usr/local/bin/cycle_hardware.sh

# We run as root to allow socat to create /dev entries
# trivy:ignore:AVD-DS-0002
# trivy:ignore:AVD-DS-0026
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
