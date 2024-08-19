package org.mycompany.hris.configuration

import io.ktor.server.application.Application
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.jetbrains.exposed.sql.Database
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.mycompany.hris.employee.EmployeeRepository
import org.mycompany.hris.employee.EmployeeService

fun Application.configureDi() {
    di {
        bindEagerSingleton<MeterRegistry> { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
        bindEagerSingleton<Database> { configureDatabase(instance()) }
        bindSingleton<EmployeeRepository> { EmployeeRepository() }
        bindSingleton<EmployeeService> { EmployeeService(instance()) }
    }
}
