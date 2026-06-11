const fs = require('fs');
const path = require('path');

/**
 * Heritage Document Guard: High-Fidelity Documentation Validator
 * 
 * Ensures that the public record remains in synchronization with the architectural state.
 */

console.log('[Heritage] Initiating Strategic Documentation Audit...');

const docsDir = 'docs';
const designDir = path.join(docsDir, 'design');
const indexFile = path.join(docsDir, 'index.md');

const audit = {
    links: { status: 'Passed', missing: [] },
    terminology: { status: 'Passed', violations: [] },
    synchronization: { status: 'Passed' }
};

// 1. Verify Design Phase Links
if (fs.existsSync(designDir) && fs.existsSync(indexFile)) {
    const indexContent = fs.readFileSync(indexFile, 'utf8');
    const designFiles = fs.readdirSync(designDir)
        .filter(f => f.startsWith('DESIGN_PHASE_') && f.endsWith('.md'))
        .sort((a, b) => {
            const numA = parseInt(a.match(/\d+/)[0]);
            const numB = parseInt(b.match(/\d+/)[0]);
            return numA - numB;
        });

    designFiles.forEach(file => {
        const linkPattern = new RegExp(`\\[Phase \\d+:.*\\]\\(design/${file}\\)`, 'i');
        if (!linkPattern.test(indexContent)) {
            audit.links.missing.push(file);
            audit.links.status = 'FAILED';
        }
    });
}

// 2. Scan for Deprecated Terminology
const deprecatedTerms = [
    { term: /ci container/gi, replacement: 'phantom guard' },
    { term: /docker\/ci/gi, replacement: 'docker/phantom' },
    { term: /Dockerfile\.ci/gi, replacement: 'docker/phantom/Dockerfile' },
    { term: /service:\s*`?ci`/gi, replacement: 'service: `phantom`' },
    { term: /`?ci`?-?container/gi, replacement: '`phantom` container' },
    { term: /run --rm ci/gi, replacement: 'run --rm phantom' },
    { term: /\bci\s+image\b/gi, replacement: 'phantom image' }
];

const walk = (dir) => {
    if (!fs.existsSync(dir)) return;
    fs.readdirSync(dir).forEach(file => {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) return walk(fullPath);
        if (!file.endsWith('.md') && !file.endsWith('.yml')) return;

        const content = fs.readFileSync(fullPath, 'utf8');
        deprecatedTerms.forEach(({ term, replacement }) => {
            const matches = content.match(term);
            if (matches) {
                audit.terminology.violations.push(`${fullPath} (${matches.length} matches for '${term.source}')`);
                audit.terminology.status = 'FAILED';
            }
        });
    });
};

walk(docsDir);
walk('.'); // Also check project root files like README.md

// 3. Final Report
console.log('\n=================================================');
console.log('🛡️  HERITAGE DOCUMENTATION READINESS REPORT');
console.log('=================================================');
console.log(`Design Phase Links: [${audit.links.status}]`);
if (audit.links.missing.length > 0) {
    console.warn('  Missing Links in index.md:');
    audit.links.missing.forEach(m => console.warn(`    - ${m}`));
}

console.log(`Terminology Audit:  [${audit.terminology.status}]`);
if (audit.terminology.violations.length > 0) {
    console.warn('  Deprecated Terminology Detected:');
    audit.terminology.violations.forEach(v => console.warn(`    - ${v}`));
}

const finalStatus = (audit.links.status === 'Passed' && audit.terminology.status === 'Passed') ? 0 : 1;
if (finalStatus === 0) {
    console.log('\n✅ Documentation is High-Fidelity and Synchronized.');
} else {
    console.error('\n❌ Documentation Audit Failed. Refactoring required.');
}

process.exit(finalStatus);
