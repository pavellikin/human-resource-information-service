package org.mycompany.hris.configuration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.micrometer.core.instrument.MeterRegistry
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabase(prometheusMeterRegistry: MeterRegistry): Database {
    val dbConfig = environment.config.config("datasource")
    val hikariConfig =
        HikariConfig().apply {
            jdbcUrl = dbConfig.property("url").getString()
            username = dbConfig.property("username").getString()
            password = dbConfig.property("password").getString()
            poolName = dbConfig.property("poolName").getString()
            maximumPoolSize = dbConfig.property("maximumPoolSize").getString().toInt()
            metricRegistry = prometheusMeterRegistry
            connectionTimeout = dbConfig.property("connectionTimeout").getString().toLong()
        }
    val dataSource = HikariDataSource(hikariConfig)
    Runtime.getRuntime().addShutdownHook(Thread { dataSource.close() })
    return Database.connect(
        datasource = dataSource,
    )
}
