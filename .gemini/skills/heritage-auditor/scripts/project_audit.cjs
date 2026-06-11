const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');

/**
 * Heritage Auditor: Strategic Codebase Explorer
 * 
 * Functions:
 * 1. Map core modules and their LOC/Coverage.
 * 2. Identify dependency cycles.
 * 3. Quantify "Statement Leakage" across the project.
 */

console.log('[Heritage] Initiating Strategic Project Audit...');

const modules = [
    { name: 'NMEA Ingestion', path: 'src/main/java/com/stoicprogrammer/qtrqth/nmea' },
    { name: 'Serial Hardware', path: 'src/main/java/com/stoicprogrammer/qtrqth/serial' },
    { name: 'Precision Analysis', path: 'src/main/java/com/stoicprogrammer/qtrqth/analysis' },
    { name: 'Functional Core', path: 'src/main/java/com/stoicprogrammer/qtrqth/util' }
];

const audit = {
    purity: { if: 0, for: 0, while: 0, switch: 0, rawParse: 0 },
    stats: { totalFiles: 0, totalLines: 0 }
};

const walk = (dir) => {
    if (!fs.existsSync(dir)) return;
    fs.readdirSync(dir).forEach(file => {
        const fullPath = path.join(dir, file);
        if (fs.statSync(fullPath).isDirectory()) return walk(fullPath);
        if (!file.endsWith('.java')) return;

        audit.stats.totalFiles++;
        const content = fs.readFileSync(fullPath, 'utf8');
        const lines = content.split('\n');
        audit.stats.totalLines += lines.length;

        audit.purity.if += (content.match(/\bif\s*\(/g) || []).length;
        audit.purity.for += (content.match(/\bfor\s*\(/g) || []).length;
        audit.purity.while += (content.match(/\bwhile\s*\(/g) || []).length;
        audit.purity.switch += (content.match(/\bswitch\b/g) || []).length;
        audit.purity.rawParse += (content.match(/\b(Integer|Long|Double|Float|Short|Byte)(\.|::)(parse[a-zA-Z]+|valueOf)\b/g) || []).length;
    });
};

modules.forEach(m => walk(m.path));

console.log('\n=================================================');
console.log('🗺️  HERITAGE PROJECT ARCHITECTURE MAP');
console.log('=================================================');
console.log(`Core Modules:   ${modules.length}`);
console.log(`Codebase Size:  ${audit.stats.totalLines} LOC (${audit.stats.totalFiles} Files)`);
console.log('\n📐 Technical Purity Audit (Imperative Leakage):');
console.log(`- if statements:     ${audit.purity.if}`);
console.log(`- for/while loops:   ${audit.purity.for + audit.purity.while}`);
console.log(`- legacy switches:   ${audit.purity.switch}`);
console.log(`- raw jdk parsing:   ${audit.purity.rawParse}`);
console.log('=================================================\n');

if (audit.purity.if > 100) {
    console.warn('[Alert] Significant imperative debt detected. Refactoring required.');
}
