package org.mycompany.hris.configuration

import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.mycompany.hris.MIGRATIONS_DIRECTORY

fun Application.configureFlyway() {
    // The idea here to separate main and DB migration pods.
    // Flyway enabled will be false for the main pods and true for the migration one.
    // Db migration pod needs to be running in one copy only before main pods rollout to apply migrations and exit.
    // Exit part is omitted here for simplicity - you can run the main logic and apply migration simultaneously.
    val isFlywayEnabled = environment.config.config("flyway").property("enabled").getString().toBooleanStrict()
    if (!isFlywayEnabled) {
        return
    }
    val dbConfig = environment.config.config("datasource")
    val url = dbConfig.property("url").getString()
    val username = dbConfig.property("username").getString()
    val password = dbConfig.property("password").getString()
    Flyway.configure()
        .dataSource(url, username, password)
        .locations("filesystem:$MIGRATIONS_DIRECTORY")
        .baselineOnMigrate(true)
        .load()
        .migrate()
}
