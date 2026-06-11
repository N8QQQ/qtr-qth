# Mechanar (WSL2) Benchmarks

## Initial Baseline (2026-06-08)
- **Environment**: Virtualized (WSL2, Ubuntu 24.04)
- **Host Context**: Windows 11 Pro
- **Hardware**: u-blox 7 via USB passthrough
- **Results**: 
  - Avg Offset: ~15-25ms
  - RMS Jitter: ~18ms
  - Observation: Jitter is significantly lower than the bare-metal Gandalf baseline (~70ms). This suggests the virtualization layer and host interrupt handling are highly performant.
