# Security Policy

## Supported Versions

The following versions of `qtr-qth` are currently being supported with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 0.4.x   | :white_check_mark: |
| < 0.4   | :x:                |

## Reporting a Vulnerability

We take the security of `qtr-qth` seriously. If you believe you have found a security vulnerability, please do **not** report it via a public issue. Instead, please follow the steps below:

1.  **Email the Maintainer:** Send an email to `security@stoicprogrammer.com` (Note: Replace with actual contact if available, or use GitHub Private Vulnerability Reporting).
2.  **Provide Details:** Include a detailed description of the vulnerability, steps to reproduce, and any potential impact.
3.  **Wait for Response:** We will acknowledge your report within 48 hours and provide a timeline for a fix.

## Security Standards

This project adheres to the following security standards:
- **SLSA Level 3:** Build integrity and non-falsifiable provenance.
- **CodeQL Advanced Scanning:** Continuous static analysis for common vulnerabilities.
- **Dependency Guarding:** Automated scanning via Dependabot.
- **Cryptographic Signing:** All releases are sealed with SSH-based git signatures.
