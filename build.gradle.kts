plugins {
    kotlin("jvm") version "2.0.10"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("io.ktor.plugin") version "2.3.12"
}

group = "org.mycompany"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // ktor
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-call-id")
    implementation("io.ktor:ktor-server-swagger")

    // metrics
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.micrometer:micrometer-registry-prometheus:1.13.3")

    // di
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:7.22.0")

    // logs
    implementation("ch.qos.logback:logback-classic:1.5.7")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
