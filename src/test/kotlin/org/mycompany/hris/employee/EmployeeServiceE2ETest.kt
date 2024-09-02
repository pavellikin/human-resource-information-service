package org.mycompany.hris.employee

import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.Test
import org.mycompany.hris.AbstractE2eTest
import org.mycompany.hris.configuration.tables.EmployeesTable
import org.mycompany.hris.employee.model.CreateEmployeeRequest
import org.mycompany.hris.employee.model.CreateEmployeeResponse
import org.mycompany.hris.employee.model.GetEmployeeResponse
import org.mycompany.hris.employee.model.PatchEmployeeRequest
import org.mycompany.hris.givenEmail
import org.mycompany.hris.givenEmployeeId
import org.mycompany.hris.givenName
import org.mycompany.hris.givenPosition
import org.mycompany.hris.givenSurname
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.Position
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EmployeeServiceE2ETest : AbstractE2eTest() {
    @Test
    fun `create employee`() =
        e2eTest {
            val client = configureClient()
            val request =
                givenCreateEmployeeRequest().also {
                    prepareDataForRequest(it.supervisor, it.subordinates)
                }

            val response =
                client.post("/api/v1/hris/employees") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            assertEquals(HttpStatusCode.Created, response.status)
            newSuspendedTransaction {
                val employeeId = response.body<CreateEmployeeResponse>().employeeId.value
                with(EmployeesTable) {
                    selectAll().where(id eq employeeId).toList().also { dbEmployees ->
                        assertEquals(1, dbEmployees.size)
                        assertEquals(employeeId, dbEmployees.first()[id])
                        assertEquals(request.name.value, dbEmployees.first()[name])
                        assertEquals(request.surname.value, dbEmployees.first()[surname])
                        assertEquals(request.email.value, dbEmployees.first()[email])
                        assertEquals(request.position.name, dbEmployees.first()[position])
                        assertEquals(request.supervisor!!.value, dbEmployees.first()[supervisor])
                    }
                }
            }
        }

    @Test
    fun `create placeholder employee`() =
        e2eTest {
            val client = configureClient()
            val request = givenCreateEmployeeRequest().copy(supervisor = null, subordinates = null)

            val response =
                client.post("/api/v1/hris/employees") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            assertEquals(HttpStatusCode.Created, response.status)
            newSuspendedTransaction {
                val employeeId = response.body<CreateEmployeeResponse>().employeeId.value
                with(EmployeesTable) {
                    selectAll().where(id eq employeeId).toList().also { dbEmployees ->
                        assertEquals(1, dbEmployees.size)
                        assertEquals(employeeId, dbEmployees.first()[id])
                        assertEquals(request.name.value, dbEmployees.first()[name])
                        assertEquals(request.surname.value, dbEmployees.first()[surname])
                        assertEquals(request.email.value, dbEmployees.first()[email])
                        assertEquals(request.position.name, dbEmployees.first()[position])
                        assertNull(dbEmployees.first()[supervisor])
                    }
                }
            }
        }

    @Test
    fun `patch employee`() =
        e2eTest {
            val client = configureClient()
            val createEmployeeRequest =
                givenCreateEmployeeRequest().also {
                    prepareDataForRequest(it.supervisor, it.subordinates)
                }
            val newPosition = Position.CTO
            val patchEmployeeRequest = givenPatchEmployeeRequest(position = newPosition)

            val employeeId =
                client.post("/api/v1/hris/employees") {
                    contentType(ContentType.Application.Json)
                    setBody(createEmployeeRequest)
                }.body<CreateEmployeeResponse>().employeeId.value
            val patchResponse =
                client.patch("/api/v1/hris/employees/$employeeId") {
                    contentType(ContentType.Application.Json)
                    setBody(patchEmployeeRequest)
                }

            assertEquals(HttpStatusCode.OK, patchResponse.status)
            newSuspendedTransaction {
                with(EmployeesTable) {
                    selectAll().where(id eq employeeId).toList().also { dbEmployees ->
                        assertEquals(1, dbEmployees.size)
                        assertEquals(employeeId, dbEmployees.first()[id])
                        assertEquals(newPosition.name, dbEmployees.first()[position])
                        // supervisor stay the same
                        assertEquals(createEmployeeRequest.supervisor!!.value, dbEmployees.first()[supervisor])
                    }
                }
            }
        }

    @Test
    fun `get employee`() =
        e2eTest {
            val client = configureClient()
            val createEmployeeRequest =
                givenCreateEmployeeRequest().also {
                    prepareDataForRequest(it.supervisor, it.subordinates)
                }

            val employeeId =
                client.post("/api/v1/hris/employees") {
                    contentType(ContentType.Application.Json)
                    setBody(createEmployeeRequest)
                }.body<CreateEmployeeResponse>().employeeId.value
            val getResponse =
                client.get("/api/v1/hris/employees/$employeeId") {
                    contentType(ContentType.Application.Json)
                }

            assertEquals(HttpStatusCode.OK, getResponse.status)
            getResponse.body<GetEmployeeResponse>().also {
                assertEquals(employeeId, it.employeeId.value)
                assertEquals(createEmployeeRequest.name, it.name)
                assertEquals(createEmployeeRequest.surname, it.surname)
                assertEquals(createEmployeeRequest.position, it.position)
                assertEquals(createEmployeeRequest.supervisor, it.supervisor)
                assertEquals(createEmployeeRequest.subordinates?.size, it.numberOfSubordinates)
            }
        }

    @Test
    fun `get unknown employee`() =
        e2eTest {
            val client = configureClient()

            val getResponse =
                client.get("/api/v1/hris/employees/${UUID.randomUUID()}") {
                    contentType(ContentType.Application.Json)
                }

            assertEquals(HttpStatusCode.NotFound, getResponse.status)
        }

    @Test
    fun `delete employee`() =
        e2eTest {
            val client = configureClient()
            val createEmployeeRequest =
                givenCreateEmployeeRequest().also {
                    prepareDataForRequest(it.supervisor, it.subordinates)
                }
            val supervisor = checkNotNull(createEmployeeRequest.supervisor)
            val subordinates = checkNotNull(createEmployeeRequest.subordinates).map { it.value }

            val employeeId =
                client.post("/api/v1/hris/employees") {
                    contentType(ContentType.Application.Json)
                    setBody(createEmployeeRequest)
                }.body<CreateEmployeeResponse>().employeeId.value
            val response =
                client.delete("/api/v1/hris/employees/$employeeId") {
                    contentType(ContentType.Application.Json)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            newSuspendedTransaction {
                with(EmployeesTable) {
                    selectAll().where(id eq employeeId).toList().also { dbEmployees ->
                        assertTrue(dbEmployees.isEmpty())
                    }
                    selectAll().where(id inList subordinates)
                        .map { it[EmployeesTable.supervisor] }
                        .onEach { assertEquals(it, supervisor.value) }
                }
            }
        }

    private suspend fun prepareDataForRequest(
        supervisor: EmployeeId? = null,
        subordinates: Set<EmployeeId>? = null,
    ) = newSuspendedTransaction {
        supervisor?.let { prefillDbForEmployee(it) }
        subordinates?.forEach { prefillDbForEmployee(it) }
    }

    private fun givenCreateEmployeeRequest() =
        CreateEmployeeRequest(
            givenName(),
            givenSurname(),
            givenEmail(),
            givenPosition(),
            givenEmployeeId(),
            setOf(givenEmployeeId()),
        )

    private fun prefillDbForEmployee(employeeId: EmployeeId) {
        EmployeesTable.insert { st ->
            st[id] = employeeId.value
            st[name] = "Name"
            st[surname] = "Surname"
            st[email] = "name.surname@mycompany.com"
            st[position] = Position.CEO.name
        }
    }

    private fun givenPatchEmployeeRequest(
        position: Position? = null,
        supervisor: EmployeeId? = null,
        subordinates: Set<EmployeeId>? = null,
    ) = PatchEmployeeRequest(
        position = position,
        supervisor = supervisor,
        subordinates = subordinates,
    )
}
