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
import org.mycompany.hris.orgchart.CacheConfig
import org.mycompany.hris.orgchart.OrgChartConfig
import org.mycompany.hris.orgchart.OrgChartRepository
import org.mycompany.hris.orgchart.OrgChartService
import org.mycompany.hris.performancereview.PerformanceReviewRepository
import org.mycompany.hris.performancereview.PerformanceReviewService
import java.time.Duration

fun Application.configureDi() {
    val config = environment.config
    di {
        bindEagerSingleton<MeterRegistry> { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
        bindEagerSingleton<Database> { configureDatabase(instance()) }
        bindSingleton<EmployeeRepository> { EmployeeRepository() }
        bindSingleton<EmployeeService> { EmployeeService(instance()) }
        bindSingleton<OrgChartRepository> { OrgChartRepository() }
        bindSingleton<OrgChartConfig> {
            OrgChartConfig(
                CacheConfig(
                    maxSize = config.config("orgChart.cache").property("maxSize").getString().toLong(),
                    expireTime = config.config("orgChart.cache").property("expireTime").getString().let { Duration.parse(it) },
                ),
            )
        }
        bindSingleton<OrgChartService> { OrgChartService(instance(), instance()) }
        bindSingleton<PerformanceReviewRepository> { PerformanceReviewRepository() }
        bindSingleton<PerformanceReviewService> { PerformanceReviewService(instance(), instance()) }
    }
}
