const fs = require('fs');
const path = require('path');

/**
 * Heritage Tracker: Automated Flight Log Management
 */
const SESSIONS_PATH = path.join(process.cwd(), 'docs/roadmap/SESSIONS.md');

const commands = {
    start: (charter) => {
        console.log(`[Heritage] Launching session: ${charter}`);
        const timestamp = new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' });
        const dateStr = new Date().toISOString().split('T')[0];
        
        let content = fs.readFileSync(SESSIONS_PATH, 'utf8');
        
        // 1. Update the table
        const tableEntry = `| 10 | ${dateStr} | [TBD] | [TBD] | [TBD] | ${charter} |\n`;
        content = content.replace(/(\| Sprint \| Date \| Total SP \| Total Hours \| SP\/Hour \| Notes \|\n\| :--- \| :--- \| :--- \| :--- \| :--- \| :--- \|\n)/, `$1${tableEntry}`);

        // 2. Insert the entry block
        const entryBlock = `    ---

    ### Session ${dateStr}: ${charter}
    - **Charter:** ${charter}
    - **Duration:** [Pending] Hours
    - **Timeline:**
        - **${timestamp}:** Session Start.
    - **Outcome:**
        - [ ] Task 1
    - **Calibration:**
        - **Estimated SP:** 10
        - **Actual Hours:** [Pending]
        - **Velocity:** [Pending] SP/Hour

`;
        content = content.replace(/(### Session \d{4}-\d{2}-\d{2}:)/, `${entryBlock}$1`);

        fs.writeFileSync(SESSIONS_PATH, content);
        console.log(`[Success] Flight Log updated for ${dateStr}.`);
    }
};

const [,, cmd, ...args] = process.argv;
if (commands[cmd]) {
    commands[cmd](args.join(' '));
} else {
    console.error(`Unknown command: ${cmd}`);
    process.exit(1);
}
