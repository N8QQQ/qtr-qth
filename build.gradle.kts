plugins {
    id("java")
    application
}

group = "com.stoicprogrammer"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Serial Communication
    implementation("com.fazecast:jSerialComm:2.11.4")
    // Network Time Protocol
    implementation("commons-net:commons-net:3.13.0")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.stoicprogrammer.qtrqth.Main")
}

tasks.test {
    useJUnitPlatform()
}
