package org.mycompany.hris

import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.jackson.jackson
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.config.mergeWith
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mycompany.hris.configuration.tables.EmployeesTable
import org.testcontainers.containers.PostgreSQLContainer

abstract class AbstractE2eTest {
    companion object {
        val postgresContainer: PostgreSQLContainer<*> =
            PostgreSQLContainer("postgres:16.4")
                .withDatabaseName("human_resource_information")
                .withUsername("postgres")
                .withPassword("postgres")
                .also {
                    it.start()
                    Runtime.getRuntime().addShutdownHook(Thread { it.stop() })
                }
    }

    fun ApplicationTestBuilder.configureClient() =
        createClient {
            install(Logging) {
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                jackson()
            }
        }

    fun prepareDb(sql: String) {
        transaction { exec(sql) }
    }

    fun e2eTest(block: suspend ApplicationTestBuilder.() -> Unit) =
        runBlocking {
            withTimeout(10_000) {
                val cl = this.javaClass.classLoader
                val testConfig =
                    ApplicationConfig(cl.getResource("application.conf").file)
                        .mergeWith(
                            MapApplicationConfig(
                                "datasource.url" to postgresContainer.getJdbcUrl(),
                                "datasource.username" to postgresContainer.username,
                                "datasource.password" to postgresContainer.password,
                                "logging.root" to "TRACE",
                            ),
                        )

                testApplication {
                    environment { config = testConfig }
                    startApplication()
                    block()
                    transaction {
                        EmployeesTable.deleteAll()
                    }
                }
            }
        }
}