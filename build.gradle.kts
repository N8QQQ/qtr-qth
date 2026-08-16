plugins {
    id("java")
    application
    jacoco
    checkstyle
    id("me.champeau.jmh") version "0.7.3"
}

group = "com.stoicprogrammer"
version = "0.8.0"

repositories {
    mavenCentral()
}

jmh {
    jmhVersion.set("1.37")
    duplicateClassesStrategy.set(DuplicatesStrategy.EXCLUDE)
}

checkstyle {
    toolVersion = "10.15.0"
    configFile = file("config").resolve("checkstyle").resolve("checkstyle.xml")
    isIgnoreFailures = false
    isShowViolations = true
}

dependencies {
    // Logging Facade & Engine
    implementation("org.slf4j:slf4j-api:2.0.18")
    runtimeOnly("ch.qos.logback:logback-classic:1.6.2")

    // Serial Communication
    implementation("com.fazecast:jSerialComm:2.11.4")
    // Network Time Protocol
    implementation("commons-net:commons-net:3.13.0")

    // Functional Ecosystem (The Hybrid Weaver)
    implementation("io.vavr:vavr:1.0.1")

    // Testing
    testImplementation(platform("org.junit:junit-bom:6.1.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.23.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


application {
    mainClass.set("com.stoicprogrammer.qtrqth.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
    classDirectories.setFrom(files(classDirectories.files.map {
        fileTree(it) {
            exclude(
                "com/stoicprogrammer/qtrqth/Main.class",
                "com/stoicprogrammer/qtrqth/serial/jserialcomm/**",
                "com/stoicprogrammer/qtrqth/serial/simulation/**"
            )
        }
    }))
}

tasks.register<JavaExec>("probeHardware") {
    group = "Verification"
    description = "Scans for physical and virtual GPS serial hardware."
    mainClass.set("com.stoicprogrammer.qtrqth.serial.HardwareProbe")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}
