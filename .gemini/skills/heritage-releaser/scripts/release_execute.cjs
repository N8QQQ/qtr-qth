const { spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

/**
 * Heritage Release Executor: Platform-Agnostic Release Engine
 * 
 * Orchestrates the final cryptographic seal and publication of a release.
 */

console.log('--- 🚀 Heritage Release Executor: Initiating Final Seal ---');

// 1. Environment Verification
const checkCommand = (cmd, args) => {
    const proc = spawnSync(cmd, args, { shell: true });
    return proc.status === 0;
};

if (!checkCommand('git', ['--version']) || !checkCommand('gh', ['--version'])) {
    console.error('❌ Critical Error: git and gh (GitHub CLI) must be installed and in PATH.');
    process.exit(1);
}

// 2. Branch & State Verification
const currentBranch = spawnSync('git', ['branch', '--show-current'], { encoding: 'utf8' }).stdout.trim();
if (currentBranch !== 'main') {
    console.error(`❌ Release Aborted: Must be on 'main' branch (Current: ${currentBranch}).`);
    process.exit(1);
}

const status = spawnSync('git', ['status', '--porcelain'], { encoding: 'utf8' }).stdout.trim();
if (status !== '') {
    console.error('❌ Release Aborted: Uncommitted changes detected. Clean your workspace before sealing.');
    process.exit(1);
}

// 3. Metadata Extraction
const buildGradle = fs.readFileSync('build.gradle.kts', 'utf8');
const versionMatch = buildGradle.match(/version\s*=\s*"([^"]+)"/);
if (!versionMatch) {
    console.error('❌ Error: Could not extract version from build.gradle.kts.');
    process.exit(1);
}
const version = `v${versionMatch[1]}`;
const notesPath = `docs/release_notes_${version}.md`;

if (!fs.existsSync(notesPath)) {
    console.error(`❌ Error: Release notes missing at ${notesPath}`);
    process.exit(1);
}

console.log(`Targeting Version: ${version}`);
console.log(`Notes Source:     ${notesPath}`);

// 4. Sunday Best Certification (via Phantom Guard)
console.log('\n[1/3] Executing Mandatory Quality Gate (Phantom Guard)...');
const verifier = spawnSync('node', ['.gemini/skills/heritage-verifier/scripts/verify_health.cjs'], { stdio: 'inherit', shell: true });

if (verifier.status !== 0) {
    console.error('\n❌ Quality Gate Failed. Release Aborted.');
    process.exit(1);
}

// 5. Cryptographic Tagging
console.log('\n[2/3] Applying Cryptographic Seal (Signed Tag)...');
const tag = spawnSync('git', ['tag', '-s', version, '-m', `release: ${version} definitive baseline`], { stdio: 'inherit', shell: true });
if (tag.status !== 0) {
    console.error('❌ Tagging Failed. Ensure signing keys are configured.');
    process.exit(1);
}

const pushTag = spawnSync('git', ['push', 'origin', version], { stdio: 'inherit', shell: true });
if (pushTag.status !== 0) {
    console.error('❌ Failed to push tag to origin.');
    process.exit(1);
}

// 6. GitHub Publication
console.log('\n[3/3] Publishing High-Fidelity Release to GitHub...');
const artifactPath = `build/distributions/qtr-qth-${version.replace('v', '')}.zip`;

if (!fs.existsSync(artifactPath)) {
    console.log('Generating distribution artifact...');
    spawnSync('./gradlew', ['distZip'], { stdio: 'inherit', shell: true });
}

const ghRelease = spawnSync('gh', [
    'release', 'create', version,
    '--title', version,
    '--notes-file', notesPath
], { stdio: 'inherit', shell: true });

if (ghRelease.status !== 0) {
    console.error('❌ GitHub Release creation failed.');
    process.exit(1);
}

const ghUpload = spawnSync('gh', [
    'release', 'upload', version,
    artifactPath
], { stdio: 'inherit', shell: true });

if (ghUpload.status !== 0) {
    console.error('❌ Artifact upload failed.');
    process.exit(1);
}

console.log(`\n✅ Heritage Release ${version} Published Successfully.`);
console.log('The telemetry rivers are now officially synchronized.');
