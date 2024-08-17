plugins {
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

    // DB
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-migration:$exposedVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.postgresql:postgresql:42.7.3")

    // di
    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:7.22.0")

    // logs
    implementation("ch.qos.logback:logback-classic:1.5.7")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.koverXmlReport)
}

kover {
    reports {
        filters {
            excludes {
                classes("MainKt")
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

val postgresUrl = "jdbc:postgresql://localhost:5442/human_resource_information"
val postgresUser = "postgres"
val postgresPassword = "postgres"

tasks.register<JavaExec>("generateMigrationScripts") {
    environment(
        "POSTGRES_MIGRATE" to true,
        "POSTGRES_URL" to postgresUrl,
        "POSTGRES_USER" to postgresUser,
        "POSTGRES_PASSWORD" to postgresPassword
    )
    group = "application"
    description = "Generate migration scripts in the path exposed-migration/migrations"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org/mycompany/GenerateMigrationScriptsKt"
}
