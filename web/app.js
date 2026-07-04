document.addEventListener("DOMContentLoaded", () => {
    const rawStream = document.getElementById("raw-stream");
    const statusLight = document.getElementById("status-light");
    const statusText = document.getElementById("status-text");
    const statNmeaType = document.getElementById("stat-nmea-type");
    const statOffset = document.getElementById("stat-offset");
    const statGrid = document.getElementById("stat-grid");
    const statSats = document.getElementById("stat-sats");

    const maxLines = 50;
    
    // NMEA Templates matching actual GPZDA, GPRMC, GPGGA structures
    const nmeaSentences = [
        { type: "$GPZDA", data: "$GPZDA,232810.00,02,04,2026,00,00*6C", grid: "FN20is", sats: 9, offset: "0.12 ms" },
        { type: "$GPRMC", data: "$GPRMC,232810.00,A,4044.2785,N,07359.6432,W,0.082,,020426,,,A*7C", grid: "FN20is", sats: 9, offset: "0.14 ms" },
        { type: "$GPGGA", data: "$GPGGA,232810.00,4044.2785,N,07359.6432,W,1,09,1.2,24.1,M,-34.0,M,,*5C", grid: "FN20is", sats: 9, offset: "0.11 ms" },
        { type: "$GPZDA", data: "$GPZDA,232811.00,02,04,2026,00,00*6D", grid: "FN20is", sats: 10, offset: "0.08 ms" },
        { type: "$GPRMC", data: "$GPRMC,232811.00,A,4044.2790,N,07359.6429,W,0.091,,020426,,,A*7B", grid: "FN20is", sats: 10, offset: "0.09 ms" },
        { type: "$GPGGA", data: "$GPGGA,232811.00,4044.2790,N,07359.6429,W,1,10,1.1,24.2,M,-34.0,M,,*53", grid: "FN20is", sats: 10, offset: "0.07 ms" },
        { type: "$GPZDA", data: "$GPZDA,232812.00,02,04,2026,00,00*6E", grid: "FN20it", sats: 11, offset: "0.05 ms" },
        { type: "$GPRMC", data: "$GPRMC,232812.00,A,4044.2801,N,07359.6421,W,0.075,,020426,,,A*7A", grid: "FN20it", sats: 11, offset: "0.06 ms" },
        { type: "$GPGGA", data: "$GPGGA,232812.00,4044.2801,N,07359.6421,W,1,11,1.0,24.5,M,-34.0,M,,*55", grid: "FN20it", sats: 11, offset: "0.05 ms" }
    ];

    function appendLine(text, typeClass = "nmea") {
        const line = document.createElement("div");
        line.className = `log-line ${typeClass}`;
        line.textContent = text;
        rawStream.appendChild(line);
        
        // Scroll to bottom
        rawStream.scrollTop = rawStream.scrollHeight;
        
        // Prune older lines
        while (rawStream.childNodes.length > maxLines) {
            rawStream.removeChild(rawStream.firstChild);
        }
    }

    // Lifecycle variables
    let state = "BOOT";
    let bootStep = 0;
    let nmeaIndex = 0;
    let activeTicks = 0;
    let cycleCounter = 0;

    const bootLogs = [
        { text: "--- 🛰️ Phantom Shack: Initializing Virtual Hardware ---", class: "system" },
        { text: "--- 🛰️ Starting Virtual Hardware Bridge ---", class: "system" },
        { text: "✅ TCP Coordinator listening on port 9999 -> /dev/ttyUSB99", class: "info" },
        { text: "--- 🔍 Hardware Audit: Enumerating Serial Devices ---", class: "system" },
        { text: "RECOMMENDED TARGET: /dev/ttyUSB99 (Probability: 95%)", class: "info" },
        { text: "-------------------------------------------", class: "system" },
        { text: "./gradlew --no-daemon build ...", class: "system" },
        { text: "BUILD SUCCESSFUL in 4s", class: "info" },
        { text: "./gradlew --no-daemon probeHardware ...", class: "system" },
        { text: "[Port: SIM1] Descriptive: Deterministic Simulation (SIM1) - Speed: 9600 baud", class: "info" },
        { text: "--- 🚀 Launching System Orchestrator ---", class: "system" },
        { text: "[INFO] NTP Provider synchronized with pool.ntp.org", class: "info" },
        { text: "[INFO] Listening for GPS signal...", class: "info" }
    ];

    function runEngine() {
        if (state === "BOOT") {
            if (bootStep < bootLogs.length) {
                const log = bootLogs[bootStep];
                appendLine(log.text, log.class);
                bootStep++;
                setTimeout(runEngine, 400 + Math.random() * 400);
            } else {
                state = "ACTIVE";
                statusLight.className = "pulse-light green";
                statusText.textContent = "SENTINEL ACTIVE";
                setTimeout(runEngine, 500);
            }
        } 
        else if (state === "ACTIVE") {
            const item = nmeaSentences[nmeaIndex % nmeaSentences.length];
            appendLine(item.data, "nmea");
            
            // Update stats
            statNmeaType.textContent = item.type;
            statOffset.textContent = item.offset;
            statGrid.textContent = item.grid;
            statSats.textContent = `${item.sats} Satellites`;
            
            nmeaIndex++;
            activeTicks++;
            
            // Check for simulated dropout every 15 active ticks
            if (activeTicks >= 18) {
                state = "DROPOUT";
                activeTicks = 0;
                setTimeout(runEngine, 500);
            } else {
                setTimeout(runEngine, 800 + Math.random() * 300);
            }
        } 
        else if (state === "DROPOUT") {
            statusLight.className = "pulse-light red";
            statusText.textContent = "SENTINEL TRIPPED";
            
            appendLine("[WARN] TTY Connection lost! Stream Sentinel triggered.", "error");
            appendLine("[INFO] Attempting hot-swap reconnection...", "system");
            
            // Reset Stats indicator
            statOffset.textContent = "---";
            statSats.textContent = "Searching...";
            
            state = "RECOVER";
            cycleCounter = 0;
            setTimeout(runEngine, 1200);
        } 
        else if (state === "RECOVER") {
            if (cycleCounter === 0) {
                appendLine("[SENTINEL] Cycling hardware via cycle_hardware.sh...", "system");
                cycleCounter++;
                setTimeout(runEngine, 1000);
            } else if (cycleCounter === 1) {
                appendLine("[SENTINEL] Rescanning ports: /dev/ttyUSB0, /dev/ttyUSB99", "system");
                cycleCounter++;
                setTimeout(runEngine, 1200);
            } else if (cycleCounter === 2) {
                appendLine("[SENTINEL] Port acquired at 9600 baud. Restoring stream listener.", "info");
                cycleCounter++;
                setTimeout(runEngine, 800);
            } else {
                state = "ACTIVE";
                statusLight.className = "pulse-light green";
                statusText.textContent = "SENTINEL ACTIVE";
                setTimeout(runEngine, 500);
            }
        }
    }

    // Begin Simulation
    runEngine();
});
