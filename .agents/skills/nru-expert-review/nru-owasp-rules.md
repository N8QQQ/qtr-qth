# OWASP 2025 Security Vectors

Evaluate the diff exclusively against these specific vectors:

## 1. Command & Argument Injection
- *Context:* This is a local hardware CLI that interfaces with system binaries, USB bridges, and Docker.
- *Check:* Ensure no untrusted input is passed directly to `ProcessBuilder`, `Runtime.exec`, or shell invocations without strict sanitization and parameterization.

## 2. Insecure Component Usage
- *Check:* Ensure no new external dependencies are introduced without explicit justification. We have a strict preference for zero external dependencies.

## 3. Path Traversal
- *Check:* Ensure any file system reads/writes (e.g., parsing `.nmea` files or config properties) strictly validate the path and prevent directory escape sequences (`../`).
