import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.testcontainers.containers.PostgreSQLContainer

buildscript {
    dependencies {
        classpath("org.testcontainers:postgresql:1.20.1")
    }
}

plugins {
    application
    kotlin("jvm") version "2.0.10"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("org.jetbrains.kotlinx.kover") version "0.8.3"
    id("io.ktor.plugin") version "2.3.12"
}

group = "org.mycompany"
version = "1.0.0"

repositories {
    mavenCentral()
}

application {
    mainClass = "org.mycompany.hris.MainKt"
}

val exposedVersion = "0.53.0"
val flywayVersion = "10.17.1"
val testContainersVersion = "1.20.1"
dependencies {
    // ktor
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-jackson")
    implementation("io.ktor:ktor-server-cors")
    implementation("io.ktor:ktor-server-default-headers")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-call-id")
    implementation("io.ktor:ktor-server-swagger")

    // metrics
    implementation("io.ktor:ktor-server-metrics-micrometer")
    implementation("io.micrometer:micrometer-registry-prometheus:1.13.3")

    // DB
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-migration:$exposedVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // di
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:7.22.0")

    // logs
    implementation("ch.qos.logback:logback-classic:1.5.7")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // test
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("io.ktor:ktor-client-content-negotiation")
    testImplementation("io.ktor:ktor-client-logging")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:postgresql:$testContainersVersion")
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }

    finalizedBy(tasks.koverXmlReport)
}

kover {
    reports {
        filters {
            excludes {
                classes("org/mycompany/hris/MainKt")
            }
        }
        verify {
            rule {
                minBound(60)
            }
        }
    }
}

kotlin {
    jvmToolchain(21)
}

val postgresContainer: PostgreSQLContainer<*> =
    PostgreSQLContainer("postgres:16.4")
        .withDatabaseName("human_resource_information")
        .withUsername("postgres")
        .withPassword("postgres")

tasks.register<JavaExec>("generateMigrationScripts") {
    if (!postgresContainer.isRunning()) {
        postgresContainer.start()
    }

    environment(
        "POSTGRES_MIGRATE" to true,
        "POSTGRES_URL" to postgresContainer.getJdbcUrl(),
        "POSTGRES_USER" to postgresContainer.username,
        "POSTGRES_PASSWORD" to postgresContainer.password,
    )
    group = "application"
    description = "Generate migration scripts in the path exposed-migration/migrations"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org/mycompany/hris/GenerateMigrationScriptsKt"
    doLast { postgresContainer.stop() }
}
