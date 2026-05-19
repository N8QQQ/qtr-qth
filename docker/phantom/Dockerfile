# Phantom Shack: Resource-Constrained Virtual Pi
# High-fidelity development mirror (Built on top of local CI baseline)

FROM qtr-qth-ci:latest

# 1. Install Additional System Dependencies (socat for hardware spoofing)
USER root
RUN apt-get update && apt-get install -y \
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
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
