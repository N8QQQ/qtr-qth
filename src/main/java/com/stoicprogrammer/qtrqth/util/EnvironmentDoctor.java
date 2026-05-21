package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.config.AppConfig;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.ntp.NtpClient;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import com.stoicprogrammer.qtrqth.serial.jserialcomm.JSerialCommProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * High-fidelity diagnostic utility for non-technical users.
 * Audits the environment and provides prescriptive rescue commands.
 */
public final class EnvironmentDoctor {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentDoctor.class);
    
    private static final int REQUIRED_JAVA_VERSION = 21;
    private static final int NTP_DIAGNOSTIC_TIMEOUT_MS = 3000;

    private EnvironmentDoctor() {
        // Utility Class
    }

    /**
     * Executes a full system readiness audit.
     */
    public static void performCheck() {
        System.out.println("--- 🩺 qtr-qth: Environment Doctor ---");
        System.out.println("Diagnosing system health and dependencies...\n");

        checkJava();
        checkSerial();
        checkNetwork();
        checkVirtualization();
        checkSourceIntegrity();

        System.out.println("-------------------------------------------");
        System.out.println("Audit Complete. If any checks failed, follow the [RESCUE] instructions above.");
    }

    private static void checkJava() {
        final String version = System.getProperty("java.version");
        System.out.print("[JAVA] Checking Version... ");
        
        final boolean ok = version.startsWith(String.valueOf(REQUIRED_JAVA_VERSION)) 
                        || version.startsWith("1." + REQUIRED_JAVA_VERSION) // Legacy format check
                        || extractMajorVersion(version) >= REQUIRED_JAVA_VERSION;

        if (ok) {
            System.out.println("PASS (Found: " + version + ")");
        } else {
            System.out.println("FAIL");
            System.out.println("  [ISSUE] qtr-qth requires Java " + REQUIRED_JAVA_VERSION + " or higher.");
            printRescue("Java", 
                "winget install EclipseTemurin.Temurin." + REQUIRED_JAVA_VERSION,
                "sudo apt install openjdk-" + REQUIRED_JAVA_VERSION + "-jdk",
                "brew install openjdk@" + REQUIRED_JAVA_VERSION);
        }
    }

    private static void checkSerial() {
        System.out.print("[SERIAL] Checking Hardware Access... ");
        try {
            final ISerialProvider provider = new JSerialCommProvider();
            final List<?> ports = provider.getAvailablePorts();
            System.out.println("PASS (Found " + ports.size() + " port(s))");
            
            if (ports.isEmpty()) {
                System.out.println("  [INFO] No physical GPS hardware detected. Using simulation fallback is recommended.");
            }
        } catch (final Exception e) {
            System.out.println("FAIL");
            System.out.println("  [ISSUE] Could not initialize serial driver: " + e.getMessage());
            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                printRescue("Permissions", "sudo usermod -a -G dialout $USER (Then log out and back in)");
            }
        }
    }

    private static void checkNetwork() {
        System.out.print("[NETWORK] Checking NTP Connectivity... ");
        final ConfigManager cm = new ConfigManager(Paths.get("qtr-qth.properties"));
        final AppConfig config = cm.getConfig();
        final NtpClient client = new NtpClient(NTP_DIAGNOSTIC_TIMEOUT_MS);
        
        final boolean ok = client.pollDetailed(config.ntpPool()).isPresent();
        if (ok) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
            System.out.println("  [ISSUE] Could not reach NTP servers: " + config.ntpPool());
            System.out.println("  [RESCUE] Check your internet connection or firewall settings (UDP Port 123).");
        }
    }

    private static void checkVirtualization() {
        System.out.print("[DOCKER] Checking Virtualization... ");
        final boolean hasDocker = runCommand("docker --version");
        final boolean hasCompose = runCommand("docker-compose --version") || runCommand("docker compose version");

        if (hasDocker && hasCompose) {
            System.out.println("PASS (Virtual Shack Ready)");
        } else {
            System.out.println("SKIP (Docker/Compose not found)");
            System.out.println("  [INFO] Optional: Install Docker to use the 'Phantom Shack' laboratory.");
        }
    }

    private static void checkSourceIntegrity() {
        final File buildFile = new File("build.gradle.kts");
        if (!buildFile.exists()) {
            return; // Not in a source tree, skipping Gradle checks.
        }

        System.out.print("[BUILD] Checking Source Integrity (Gradle)... ");
        final String wrapperName = System.getProperty("os.name").toLowerCase().contains("win") ? "gradlew.bat" : "gradlew";
        final File wrapper = new File(wrapperName);

        if (wrapper.exists() && wrapper.canExecute()) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
            System.out.println("  [ISSUE] Gradle wrapper missing or not executable.");
            printRescue("Source", "chmod +x gradlew (Linux/Mac)");
        }
    }

    private static int extractMajorVersion(final String version) {
        try {
            final String major = version.split("\\.")[0];
            return Integer.parseInt(major);
        } catch (final Exception e) {
            return 0;
        }
    }

    private static boolean runCommand(final String cmd) {
        try {
            final Process p = Runtime.getRuntime().exec(cmd);
            return p.waitFor(2, TimeUnit.SECONDS) && p.exitValue() == 0;
        } catch (final Exception e) {
            return false;
        }
    }

    private static void printRescue(final String topic, final String... commands) {
        System.out.println("  [RESCUE: " + topic + "]");
        Arrays.stream(commands)
            .forEach(cmd -> System.out.println("    > " + cmd));
    }
}
