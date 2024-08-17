@file:OptIn(ExperimentalDatabaseMigrationApi::class)

package org.mycompany.hris

import MigrationUtils
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.sql.transactions.transaction
import org.mycompany.hris.configuration.tables.EmployeesTable

const val MIGRATIONS_DIRECTORY = "src/main/resources/db/migrations"

// Support class that will be called after ./gradlew generateMigrationScripts command.
// The class generates Flyway migration scripts from the tables definitions. This logic is separated from the main one.
// It is supposed that a developer should generate migration scripts by himself after tables update.
fun main() {
    if (System.getenv("POSTGRES_MIGRATE")?.toBoolean() == false) {
        return
    }
    val db = Database.connect(
        url = System.getenv("POSTGRES_URL"),
        user = System.getenv("POSTGRES_USER"),
        password = System.getenv("POSTGRES_PASSWORD"),
        driver = "org.postgresql.Driver"
    )
    transaction(db) {
        generateEmployeesTable()
    }
}

fun generateEmployeesTable() = MigrationUtils.generateMigrationScript(
    EmployeesTable,
    scriptDirectory = MIGRATIONS_DIRECTORY,
    scriptName = "V1__employees_table",
    withLogs = true
)

