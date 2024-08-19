package org.mycompany.hris

import io.ktor.server.application.Application
import org.mycompany.hris.configuration.configureDi
import org.mycompany.hris.configuration.configureFlyway
import org.mycompany.hris.configuration.configureRoutes
import org.mycompany.hris.configuration.configureServer

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureFlyway()
    configureDi()
    configureServer()
    configureRoutes()
}
