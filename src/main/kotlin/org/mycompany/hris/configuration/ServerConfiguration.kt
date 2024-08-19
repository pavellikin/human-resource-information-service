package org.mycompany.hris.configuration

import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callid.generate
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.slf4j.event.Level

fun Application.configureServer() {
    install(ContentNegotiation) {
        jackson()
    }
    install(DefaultHeaders)
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }
    val loggingLevel =
        environment.config.config("logging").property("root")
            .getString()
            .let { Level.valueOf(it.uppercase()) }
    install(CallLogging) {
        level = loggingLevel
        callIdMdc("spanId")
    }
    install(CallId) {
        retrieveFromHeader(HttpHeaders.XRequestId)
        generate(10, "abcde12345")
    }
    val meterRegistry by closestDI().instance<MeterRegistry>()
    install(MicrometerMetrics) {
        registry = meterRegistry
        meterBinders =
            listOf(
                JvmMemoryMetrics(),
                JvmGcMetrics(),
                ProcessorMetrics(),
            )
    }
}
