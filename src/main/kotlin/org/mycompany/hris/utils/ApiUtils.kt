package org.mycompany.hris.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.util.pipeline.PipelineContext
import org.mycompany.hris.configuration.apiLogger
import org.mycompany.hris.exception.BadRequestException
import org.mycompany.hris.exception.NotFoundException
import org.mycompany.hris.model.ErrorResponse
import org.slf4j.MDC

suspend fun PipelineContext<*, ApplicationCall>.withErrorHandling(callback: suspend () -> Unit) =
    try {
        callback()
    } catch (ex: BadRequestException) {
        apiLogger.error("Wrong request during processing", ex)
        val errorMessage = ex.message ?: "Wrong input parameters"
        call.respond(HttpStatusCode.BadRequest, ErrorResponse(errorMessage))
    } catch (ex: NotFoundException) {
        apiLogger.error("Entity not found during processing", ex)
        val errorMessage = ex.message ?: "Not found"
        call.respond(HttpStatusCode.NotFound, ErrorResponse(errorMessage))
    } catch (ex: Exception) {
        apiLogger.error("Unexpected error during processing", ex)
        val errorMessage = ex.message ?: "Internal server error"
        call.respond(HttpStatusCode.InternalServerError, ErrorResponse(errorMessage))
    } finally {
        MDC.clear()
    }

fun PipelineContext<*, ApplicationCall>.extractMandatoryPathParameter(name: String): String =
    call.parameters[name] ?: throw BadRequestException("$name is missing in path parameters")

fun PipelineContext<*, ApplicationCall>.extractMandatoryQueryParameter(name: String): String =
    extractQueryParameter(name) ?: throw BadRequestException("$name is missing in path parameters")

fun PipelineContext<*, ApplicationCall>.extractQueryParameter(name: String): String? = call.request.queryParameters[name]
