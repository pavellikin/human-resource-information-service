package org.mycompany.hris.configuration

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI

fun Application.configureRoutes() {
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
