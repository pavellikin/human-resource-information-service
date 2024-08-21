package org.mycompany.hris.configuration

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.mycompany.hris.employee.EmployeeService
import org.mycompany.hris.employee.model.CreateEmployeeRequest
import org.mycompany.hris.employee.model.PatchEmployeeRequest
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.orgchart.OrgChartService
import org.mycompany.hris.orgchart.model.Expand
import org.mycompany.hris.utils.extractMandatoryPathParameter
import org.mycompany.hris.utils.extractMandatoryQueryParameter
import org.mycompany.hris.utils.extractQueryParameter
import org.mycompany.hris.utils.withErrorHandling
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

val apiLogger: Logger = LoggerFactory.getLogger("router")

fun Application.configureRoutes() {
    routing {
        route("/api/v1/hris") {
            employeesRoutes()
            orgChartRoutes()
        }
        operationalRoutes()
    }
}

private fun Route.employeesRoutes() {
    val employeeService by closestDI().instance<EmployeeService>()
    post("/employees") {
        withErrorHandling {
            val body = call.receive(CreateEmployeeRequest::class)
            val response = employeeService.createEmployee(body)
            call.respond(HttpStatusCode.Created, response)
        }
    }
    patch("/employees/{employeeId}") {
        withErrorHandling {
            val employeeId = extractMandatoryPathParameter("employeeId").let(EmployeeId::fromString)
            val body = call.receive(PatchEmployeeRequest::class)
            employeeService.updateEmployee(employeeId, body)
            call.respond(HttpStatusCode.OK)
        }
    }
    get("/employees/{employeeId}") {
        withErrorHandling {
            val employeeId = extractMandatoryPathParameter("employeeId").let(EmployeeId::fromString)
            val response = employeeService.getEmployee(employeeId)
            call.respond(HttpStatusCode.OK, response)
        }
    }
    delete("/employees/{employeeId}") {
        val employeeId = extractMandatoryPathParameter("employeeId").let(EmployeeId::fromString)
        employeeService.deleteEmployee(employeeId)
        call.respond(HttpStatusCode.OK)
    }
}

private fun Route.orgChartRoutes() {
    val orgChartService by closestDI().instance<OrgChartService>()
    get("/organization/org-chart/all") {
        withErrorHandling {
            val response = orgChartService.getAllOrgChart()
            call.respond(HttpStatusCode.OK, response)
        }
    }
    get("/organization/org-chart") {
        withErrorHandling {
            val employeeId = extractMandatoryQueryParameter("employeeId").let(EmployeeId::fromString)
            val expand = extractQueryParameter("expand")?.let(Expand::fromString) ?: Expand.None
            val step = extractQueryParameter("step")?.toInt() ?: 0
            MDC.getCopyOfContextMap().putAll(
                mapOf(
                    "employeeId" to employeeId.toString(),
                    "expand" to expand.name,
                ),
            )
            val response = orgChartService.getEmployeeOrgChart(employeeId, expand, step)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}

private fun Route.operationalRoutes() {
    val di = closestDI()
    swaggerUI(path = "openapi", swaggerFile = "openapi/human-resource-information-service.yaml")
    get("/metrics") {
        val registry by di.instance<PrometheusMeterRegistry>()
        call.respond(registry.scrape())
    }
    get("/health") {
        call.respond(HttpStatusCode.OK)
    }
}
