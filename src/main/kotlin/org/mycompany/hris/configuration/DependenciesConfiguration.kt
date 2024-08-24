package org.mycompany.hris.configuration

import io.ktor.server.application.Application
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.jetbrains.exposed.sql.Database
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.ktor.di
import org.mycompany.hris.employee.EmployeeRepository
import org.mycompany.hris.employee.EmployeeService
import org.mycompany.hris.orgchart.OrgChartRepository
import org.mycompany.hris.orgchart.OrgChartService
import org.mycompany.hris.performancereview.PerformanceReviewRepository
import org.mycompany.hris.performancereview.PerformanceReviewService

fun Application.configureDi() {
    di {
        bindEagerSingleton<PrometheusMeterRegistry> { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
        bindEagerSingleton<Database> { configureDatabase(instance()) }
        bindSingleton<EmployeeRepository> { EmployeeRepository() }
        bindSingleton<EmployeeService> { EmployeeService(instance()) }
        bindSingleton<OrgChartRepository> { OrgChartRepository() }
        bindSingleton<OrgChartService> { OrgChartService(instance()) }
        bindSingleton<PerformanceReviewRepository> { PerformanceReviewRepository() }
        bindSingleton<PerformanceReviewService> { PerformanceReviewService(instance(), instance()) }
    }
}
