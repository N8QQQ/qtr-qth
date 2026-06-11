const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

/**
 * Heritage Releaser: Artifact & Metadata Controller
 */

console.log('[Heritage] Initiating Release Readiness Audit...');

// 1. Version Synchronization Audit
const gradleContent = fs.readFileSync('build.gradle.kts', 'utf8');
const citationContent = fs.readFileSync('CITATION.cff', 'utf8');

const gradleVersion = gradleContent.match(/version = "([\d.A-Z-]+)"/)[1];
const citationVersion = citationContent.match(/version: ([\d.A-Z-]+)/)[1];

console.log(`- build.gradle.kts: ${gradleVersion}`);
console.log(`- CITATION.cff:     ${citationVersion}`);

if (gradleVersion !== citationVersion) {
    console.error('[Failure] Metadata Mismatch! Version synchronization required.');
    process.exit(1);
}

// 2. Artifact Verification
const distZip = `build/distributions/qtr-qth-${gradleVersion}.zip`;
if (fs.existsSync(distZip)) {
    console.log(`- Artifact Verified: ${distZip}`);
} else {
    console.warn(`- Warning: Distribution ZIP not found at ${distZip}. Run ./gradlew distZip.`);
}

// 3. 7-Zip Heritage Pack (Optional)
const sevenZip = '7z';
const archiveName = `qtr-qth-heritage-pack-${gradleVersion}.zip`;
if (process.argv.includes('--pack')) {
    console.log(`[Heritage] Packaging high-fidelity assets via 7-Zip...`);
    const pack = spawnSync(sevenZip, ['a', '-tzip', archiveName, 'benchmarks/', 'docs/', distZip], { shell: true });
    if (pack.status === 0) {
        console.log(`- Heritage Pack Sealed: ${archiveName}`);
    } else {
        console.warn('- 7-Zip not found or failed. Skipping Heritage Pack.');
    }
}

console.log('\n=================================================');
console.log('🚀 HERITAGE RELEASE READINESS REPORT');
console.log('=================================================');
console.log(`Version:       v${gradleVersion}`);
console.log(`Metadata Sync: [PASSED]`);
console.log(`Artifacts:     [${fs.existsSync(distZip) ? 'VERIFIED' : 'PENDING'}]`);
console.log('=================================================');

if (fs.existsSync(distZip)) {
    console.log('\n[Action] Execute high-fidelity release sequence:');
    console.log(`  1. git tag -s v${gradleVersion} -m "release: ${gradleVersion} definitive baseline"`);
    console.log(`  2. git push origin v${gradleVersion}`);
    console.log(`  3. .\\scripts\\publish-release.ps1 -Version "v${gradleVersion}"`);
}
