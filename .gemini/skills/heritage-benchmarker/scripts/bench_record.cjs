const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

/**
 * Heritage Benchmarker: Automated Metric Extraction
 */

// Improved Platform Detection
const isWin = process.platform === 'win32';
const isWsl = !isWin && fs.existsSync('/proc/version') && fs.readFileSync('/proc/version', 'utf8').toLowerCase().includes('microsoft');
const platform = isWin ? 'athena' : (isWsl ? 'mechanar' : 'gandalf');

const gradlew = isWin ? 'gradlew.bat' : './gradlew';

console.log(`[Heritage] Initiating ${platform} hardware capture...`);

const run = spawn(gradlew, ['run', '--args="--capture --duration=30"'], { shell: true });

let stdout = '';
run.stdout.on('data', (data) => {
    stdout += data.toString();
    process.stdout.write(data);
});

run.on('close', (code) => {
    if (code !== 0) {
        console.error(`[Failure] Benchmark exited with code ${code}`);
        process.exit(code);
    }

    console.log('\n[Heritage] Capture complete. Extracting metrics...');

    // Extract Latest Pulse Metrics (Using Stability as the Jitter metric for historical baseline parity)
    const offsetMatch = [...stdout.matchAll(/Offset: PT(-?[\d.]+)S/g)].pop();
    const jitterMatch = [...stdout.matchAll(/Jitter: ([\d.]+)us/g)].pop();

    if (!offsetMatch || !jitterMatch) {
        console.error('[Failure] Could not find precision metrics in logs.');
        process.exit(1);
    }

    const offsetMs = (parseFloat(offsetMatch[1]) * 1000).toFixed(1);
    const jitterMs = (parseFloat(jitterMatch[1]) / 1000).toFixed(1);

    console.log(`[Metrics] Offset: ${offsetMs}ms | Jitter: ${jitterMs}ms`);

    // Archival
    const files = fs.readdirSync('.').filter(f => f.startsWith('telemetry_capture_') && f.endsWith('.nmea'));
    if (files.length === 0) {
        console.error('[Failure] No telemetry capture file found.');
        process.exit(1);
    }

    const newest = files.sort((a, b) => fs.statSync(b).mtime - fs.statSync(a).mtime)[0];
    const date = new Date().toISOString().split('T')[0].replace(/-/g, '');
    
    // Canonical Directory Routing
    const targetDir = isWin ? 'benchmarks/windows-workstation' : (isWsl ? 'benchmarks/mechanar' : 'benchmarks/linux-laptop');
    const targetFile = `${date}-${platform}-live-115k.nmea`;
    const targetPath = path.join(targetDir, targetFile);

    if (!fs.existsSync(targetDir)) fs.mkdirSync(targetDir, { recursive: true });
    
    fs.renameSync(newest, targetPath);
    console.log(`[Archival] Telemetry secured at: ${targetPath}`);
    console.log(`\n[Action Required] Update BENCHMARK.md with: | ${platform} | ... | ~${offsetMs}ms | ~${jitterMs}ms |`);
});
