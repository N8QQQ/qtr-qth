package com.stoicprogrammer.qtrqth.util;

import com.stoicprogrammer.qtrqth.config.AppConfig;
import com.stoicprogrammer.qtrqth.config.ConfigManager;
import com.stoicprogrammer.qtrqth.ntp.NtpClient;
import com.stoicprogrammer.qtrqth.serial.api.ISerialProvider;
import com.stoicprogrammer.qtrqth.serial.jserialcomm.JSerialCommProvider;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * High-fidelity diagnostic utility for non-technical users.
 * Audits the environment and provides prescriptive rescue commands.
 * Refactored for Technical Purity (v0.8.0).
 */
public final class EnvironmentDoctor {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentDoctor.class);
    
    private static final int REQUIRED_JAVA_VERSION = 21;
    private static final int NTP_DIAGNOSTIC_TIMEOUT_MS = 3000;
    private static final int COMMAND_TIMEOUT_SECONDS = 2;

    private EnvironmentDoctor() {
        // Utility Class
    }

    /**
     * Executes a full system readiness audit.
     */
    public static void performCheck() {
        System.out.println("--- 🩺 qtr-qth: Environment Doctor ---");
        System.out.println("Diagnosing system health and dependencies...\n");

        Stream.<Runnable>of(
            EnvironmentDoctor::checkJava,
            EnvironmentDoctor::checkSerial,
            EnvironmentDoctor::checkNetwork,
            EnvironmentDoctor::checkVirtualization,
            EnvironmentDoctor::checkSourceIntegrity
        ).forEach(Runnable::run);

        System.out.println("-------------------------------------------");
        System.out.println("Audit Complete. If any checks failed, follow the [RESCUE] instructions above.");
    }

    private static void checkJava() {
        final String version = System.getProperty("java.version");
        System.out.print("[JAVA] Checking Version... ");
        
        final boolean ok = Option.of(version)
            .map(v -> v.startsWith(String.valueOf(REQUIRED_JAVA_VERSION)) 
                   || v.startsWith("1." + REQUIRED_JAVA_VERSION) 
                   || extractMajorVersion(v) >= REQUIRED_JAVA_VERSION)
            .getOrElse(false);

        Option.of(ok)
            .filter(Boolean::booleanValue)
            .peek(o -> System.out.println("PASS (Found: " + version + ")"))
            .onEmpty(() -> {
                System.out.println("FAIL");
                System.out.println("  [ISSUE] qtr-qth requires Java " + REQUIRED_JAVA_VERSION + " or higher.");
                printRescue("Java", 
                    "winget install EclipseTemurin.Temurin." + REQUIRED_JAVA_VERSION,
                    "sudo apt install openjdk-" + REQUIRED_JAVA_VERSION + "-jdk",
                    "brew install openjdk@" + REQUIRED_JAVA_VERSION);
            });
    }

    private static void checkSerial() {
        System.out.print("[SERIAL] Checking Hardware Access... ");
        
        Try.of(() -> {
            final ISerialProvider provider = new JSerialCommProvider();
            final List<?> ports = provider.getAvailablePorts();
            System.out.println("PASS (Found " + ports.size() + " port(s))");
            return ports;
        }).onSuccess(ports -> Option.of(ports.isEmpty())
            .filter(Boolean::booleanValue)
            .peek(empty -> System.out.println("  [INFO] No physical GPS hardware detected. Using simulation fallback is recommended.")))
        .onFailure(e -> {
            System.out.println("FAIL");
            System.out.println("  [ISSUE] Could not initialize serial driver: " + e.getMessage());
            Option.of(System.getProperty("os.name").toLowerCase())
                .filter(os -> os.contains("linux"))
                .peek(os -> printRescue("Permissions", "sudo usermod -a -G dialout $USER (Then log out and back in)"));
        });
    }

    private static void checkNetwork() {
        System.out.print("[NETWORK] Checking NTP Connectivity... ");
        
        final ConfigManager cm = new ConfigManager(Paths.get("qtr-qth.properties"));
        final AppConfig config = cm.getConfig();
        final NtpClient client = new NtpClient(NTP_DIAGNOSTIC_TIMEOUT_MS);
        
        Option.of(client.pollDetailed(config.ntpPool()).isPresent())
            .filter(Boolean::booleanValue)
            .peek(ok -> System.out.println("PASS"))
            .onEmpty(() -> {
                System.out.println("FAIL");
                System.out.println("  [ISSUE] Could not reach NTP servers: " + config.ntpPool());
                System.out.println("  [RESCUE] Check your internet connection or firewall settings (UDP Port 123).");
            });
    }

    private static void checkVirtualization() {
        System.out.print("[DOCKER] Checking Virtualization... ");
        
        final boolean hasDocker = runCommand("docker --version");
        final boolean hasCompose = runCommand("docker-compose --version") || runCommand("docker compose version");

        Option.of(hasDocker && hasCompose)
            .filter(Boolean::booleanValue)
            .peek(ok -> System.out.println("PASS (Virtual Shack Ready)"))
            .onEmpty(() -> {
                System.out.println("SKIP (Docker/Compose not found)");
                System.out.println("  [INFO] Optional: Install Docker to use the 'Phantom Shack' laboratory.");
            });
    }

    private static void checkSourceIntegrity() {
        final File buildFile = new File("build.gradle.kts");
        
        Option.of(buildFile.exists())
            .filter(Boolean::booleanValue)
            .peek(exists -> {
                System.out.print("[BUILD] Checking Source Integrity (Gradle)... ");
                final String wrapperName = Option.of(System.getProperty("os.name").toLowerCase())
                    .filter(os -> os.contains("win"))
                    .map(os -> "gradlew.bat")
                    .getOrElse("gradlew");
                
                final File wrapper = new File(wrapperName);

                Option.of(wrapper.exists() && wrapper.canExecute())
                    .filter(Boolean::booleanValue)
                    .peek(ok -> System.out.println("PASS"))
                    .onEmpty(() -> {
                        System.out.println("FAIL");
                        System.out.println("  [ISSUE] Gradle wrapper missing or not executable.");
                        printRescue("Source", "chmod +x gradlew (Linux/Mac)");
                    });
            });
    }

    private static int extractMajorVersion(final String version) {
        return Try.of(() -> version.split("\\.")[0])
            .flatMap(s -> Option.ofOptional(Functional.tryParseInt(s)).toTry())
            .getOrElse(0);
    }

    private static boolean runCommand(final String cmd) {
        return Try.of(() -> Runtime.getRuntime().exec(cmd))
            .map(p -> {
                final Try<Boolean> waitResult = Try.of(() -> p.waitFor(COMMAND_TIMEOUT_SECONDS, TimeUnit.SECONDS));
                return waitResult.getOrElse(false) && p.exitValue() == 0;
            })
            .getOrElse(false);
    }

    private static void printRescue(final String topic, final String... commands) {
        System.out.println("  [RESCUE: " + topic + "]");
        Arrays.stream(commands)
            .forEach(cmd -> System.out.println("    > " + cmd));
    }
}
