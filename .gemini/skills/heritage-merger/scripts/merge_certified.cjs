const { spawnSync } = require('child_process');
const fs = require('fs');

/**
 * Heritage Merger: Cross-Platform Handoff Controller
 */

console.log('[Heritage] Initiating Merge Certification Audit...');

// 1. Branch Identity Audit
const branchRes = spawnSync('git', ['branch', '--show-current'], { encoding: 'utf8' });
const branch = branchRes.stdout.trim();

if (branch === 'main' || branch === 'master') {
    console.error('[Failure] Direct merge from main is FORBIDDEN by Heritage Protocol.');
    process.exit(1);
}

// 2. Origin Sync Audit
const fetch = spawnSync('git', ['fetch', 'origin'], { shell: true });
const status = spawnSync('git', ['status', '-uno'], { encoding: 'utf8', shell: true });

if (status.stdout.includes('Your branch is ahead')) {
    console.warn('[Warning] Local branch has unpushed commits. Sync with origin before merging.');
}

// 3. GitHub PR Detection
const prList = spawnSync('gh', ['pr', 'list', '--head', branch, '--json', 'number,title,state'], { encoding: 'utf8', shell: true });
let prId = null;

try {
    const prs = JSON.parse(prList.stdout);
    if (prs.length > 0) {
        prId = prs[0].number;
        console.log(`- Detected PR #${prId}: ${prs[0].title}`);
    } else {
        console.log('- No active PR detected for this branch. PR creation required.');
    }
} catch (e) {
    console.warn('- GH CLI unavailable or error parsing PR list.');
}

// 4. Phantom Guard Verification (Checks for recent run)
const phantomLog = 'build/test-results/test'; // Check if tests were run locally at least
if (!fs.existsSync(phantomLog)) {
    console.warn('[Warning] No local build artifacts found. Execute heritage-verifier before merging.');
}

console.log('\n=================================================');
console.log('🛡️  HERITAGE MERGE READINESS REPORT');
console.log('=================================================');
console.log(`Branch:        ${branch}`);
console.log(`PR Identity:   [${prId ? '#' + prId : 'NOT FOUND'}]`);
console.log(`Protocol Status: [PENDING USER EXECUTION]`);
console.log('=================================================');

if (prId) {
    const mergeScript = process.platform === 'win32' ? `.\\scripts\\merge-pr.ps1 -PrId ${prId}` : `bash ./scripts/merge-pr.sh -PrId ${prId}`;
    console.log('\n[Action] Finalize the Heritage lifecycle:');
    console.log(`  1. ${mergeScript}`);
    console.log(`  2. git checkout main && git pull origin main`);
} else {
    console.log('\n[Action] Create PR first:');
    console.log(`  gh pr create --title "feat: ${branch}" --body "Phase completion."`);
}
