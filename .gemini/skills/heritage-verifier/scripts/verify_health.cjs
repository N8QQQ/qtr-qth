const { spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

/**
 * Heritage Verifier: Deterministic Health Controller
 */

console.log(`[Heritage] Initiating 'Sunday Best' Health Certification (Phantom Guard)...`);

// 1. Build & Test Execution (Mandating Docker for Parity)
const build = spawnSync('docker-compose', ['run', '--rm', 'phantom', './gradlew', 'clean', 'check', 'jacocoTestReport'], { shell: true, stdio: 'inherit' });

const report = {
    tests: { status: 'Unknown', passed: 0, failed: 0 },
    checkstyle: { status: 'Unknown', violations: 0 },
    coverage: { status: 'Unknown', percentage: 0 },
    purity: { status: 'Passed', breaches: [] }
};

// 2. Parse Test Results
const testDir = 'build/test-results/test';
if (fs.existsSync(testDir)) {
    const xmls = fs.readdirSync(testDir).filter(f => f.endsWith('.xml'));
    xmls.forEach(file => {
        const content = fs.readFileSync(path.join(testDir, file), 'utf8');
        const failures = content.match(/failures="(\d+)"/);
        const total = content.match(/tests="(\d+)"/);
        if (failures) report.tests.failed += parseInt(failures[1]);
        if (total) report.tests.passed += parseInt(total[1]);
    });
    report.tests.status = report.tests.failed === 0 ? 'PASSED' : 'FAILED';
}

// 3. Parse Checkstyle
const csPath = 'build/reports/checkstyle/main.xml';
if (fs.existsSync(csPath)) {
    const content = fs.readFileSync(csPath, 'utf8');
    const violations = (content.match(/<error/g) || []).length;
    report.checkstyle.violations = violations;
    report.checkstyle.status = violations === 0 ? 'PASSED' : 'FAILED';
}

// 4. Parse Jacoco (Simple Regex for CSV/XML)
const jacocoPath = 'build/reports/jacoco/test/jacocoTestReport.xml';
if (fs.existsSync(jacocoPath)) {
    const content = fs.readFileSync(jacocoPath, 'utf8');
    // Basic calculation: total instructions covered vs missed
    const matches = [...content.matchAll(/<counter type="INSTRUCTION" missed="(\d+)" covered="(\d+)"\/>/g)];
    if (matches.length > 0) {
        let missed = 0, covered = 0;
        matches.forEach(m => {
            missed += parseInt(m[1]);
            covered += parseInt(m[2]);
        });
        report.coverage.percentage = ((covered / (covered + missed)) * 100).toFixed(2);
        report.coverage.status = report.coverage.percentage > 90 ? 'PASSED' : 'STRICT_AUDIT_REQUIRED';
    }
}

// 5. Technical Purity Audit (Static Analysis)
const srcDir = 'src/main/java';
const walk = (dir) => {
    fs.readdirSync(dir).forEach(file => {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) return walk(fullPath);
        if (!file.endsWith('.java')) return;
        
        const content = fs.readFileSync(fullPath, 'utf8');
        const imperativePatterns = [
            { regex: /\bif\s*\(/g, name: 'if-statement' },
            { regex: /\bfor\s*\(/g, name: 'for-loop' },
            { regex: /\bwhile\s*\(/g, name: 'while-loop' },
            { regex: /\bswitch\b/g, name: 'legacy-switch' }
        ];

        imperativePatterns.forEach(p => {
            if (content.match(p.regex)) {
                report.purity.status = 'BREACH_DETECTED';
                report.purity.breaches.push(`${path.relative(srcDir, fullPath)}: ${p.name}`);
            }
        });
    });
};
if (fs.existsSync(srcDir)) walk(srcDir);

// 6. Report Generation
console.log('\n=================================================');
console.log('🛡️  HERITAGE TECHNICAL READINESS CERTIFICATE');
console.log('=================================================');
console.log(`Test Status:      [${report.tests.status}] (${report.tests.passed} Passed, ${report.tests.failed} Failed)`);
console.log(`Style Compliance: [${report.checkstyle.status}] (${report.checkstyle.violations} Violations)`);
console.log(`Coverage Depth:   [${report.coverage.status}] (${report.coverage.percentage}%)`);
console.log(`Technical Purity: [${report.purity.status}]`);
if (report.purity.breaches.length > 0) {
    console.log('\n🚨 Protocol Breaches (Imperative Leakage):');
    report.purity.breaches.slice(0, 10).forEach(b => console.log(`  - ${b}`));
}
console.log('=================================================\n');

if (build.status !== 0 || report.tests.failed > 0) process.exit(1);
