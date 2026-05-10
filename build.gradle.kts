plugins {
    id("java")
    application
    jacoco
}

group = "com.stoicprogrammer"
version = "0.2.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Logging Facade & Engine
    implementation("org.slf4j:slf4j-api:2.0.12")
    runtimeOnly("ch.qos.logback:logback-classic:1.5.3")

    // Serial Communication
    implementation("com.fazecast:jSerialComm:2.11.4")
    // Network Time Protocol
    implementation("commons-net:commons-net:3.13.0")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.11.0")
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
