# Heritage Grade Local CI Runner
# Mirroring GitHub Actions Environment (Ubuntu 24.04 + JDK 21)

FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

# 1. Install Dependencies
RUN apt-get update && apt-get install -y \
    openjdk-21-jdk-headless \
    curl \
    git \
    && rm -rf /var/lib/apt/lists/*

# 2. Set Workspace
WORKDIR /workspace

# 3. Validation
# We expect the host to mount the project root to /workspace.
# Docker Compose handles the volume and execution.
CMD ["./gradlew", "clean", "checkstyleMain", "checkstyleTest", "test", "distZip", "--no-daemon"]
