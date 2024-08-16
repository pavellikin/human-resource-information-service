package org.mycompany

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.generate
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import org.kodein.di.singleton
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureDi()
    configureServer()
    configureRoutes()
}

private fun Application.configureDi() {
    di {
        bind<PrometheusMeterRegistry> {  singleton { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }}
    }
}

private fun Application.configureServer() {
    install(DefaultHeaders)
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }
    val loggingLevel = environment.config.config("logging").property("root")
        .getString()
        .let { Level.valueOf(it.uppercase()) }
    install(CallLogging) {
        level = loggingLevel
    }
    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
        generate(10, "abcde12345")
    }
    val meterRegistry by closestDI().instance<MeterRegistry>()
    install(MicrometerMetrics) {
        registry = meterRegistry
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics()
        )
    }
}

private fun Application.configureRoutes() {
    val di = closestDI()
    routing {
        swaggerUI(path = "openapi", swaggerFile = "openapi/human-resource-information-service.yaml")
        get("/metrics") {
            val registry by di.instance<PrometheusMeterRegistry>()
            call.respond(registry.scrape())
        }
        get("/health") {
            call.respond(HttpStatusCode.OK)
        }
    }
}
