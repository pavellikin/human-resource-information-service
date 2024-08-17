package org.mycompany.hris.configuration

import io.ktor.server.application.Application
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.kodein.di.bind
import org.kodein.di.ktor.di
import org.kodein.di.singleton

fun Application.configureDi() {
    di {
        bind<PrometheusMeterRegistry> { singleton { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) } }
    }
}
