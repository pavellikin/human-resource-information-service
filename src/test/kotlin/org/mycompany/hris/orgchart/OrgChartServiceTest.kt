package org.mycompany.hris.orgchart

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.jupiter.api.Test
import org.mycompany.hris.AbstractE2eTest
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.Position
import org.mycompany.hris.orgchart.model.Expand
import org.mycompany.hris.orgchart.model.OrgChartEmployee
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// All tests are based on the V2__add_employees.sql migration script
class OrgChartServiceTest : AbstractE2eTest() {
    companion object {
        val sql = this.javaClass.classLoader.getResource("db/migrations/V2__add_employees.sql").readText()
    }

    @Test
    fun `get all org chart`() =
        e2eTest {
            prepareDb(sql)
            val client = configureClient()

            val response =
                client.get("/api/v1/hris/organization/org-chart/all") {
                    contentType(ContentType.Application.Json)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            response.body<Map<UUID, OrgChartEmployee>>().let { orgChart ->
                assertEquals(11, orgChart.size)
                generateSequence(1) { it + 1 }.take(11).forEach { i ->
                    val append = i.toString().padStart(2, '0')
                    "00000000-0000-0000-0000-0000000000$append".let(UUID::fromString).also {
                        assertNotNull(orgChart[it])
                        assertNotNull(orgChart[it]?.name)
                        assertNotNull(orgChart[it]?.surname)
                        assertNotNull(orgChart[it]?.position)
                    }
                }
            }
        }

    @Test
    fun `get org chart for employee`() =
        e2eTest {
            prepareDb(sql)
            val ctoId = EmployeeId.fromString("00000000-0000-0000-0000-000000000002")
            val client = configureClient()

            val response =
                client.get("/api/v1/hris/organization/org-chart") {
                    url { parameters.append("employeeId", ctoId.toString()) }
                    contentType(ContentType.Application.Json)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            response.body<Map<UUID, OrgChartEmployee>>().let { orgChart ->
                assertEquals(5, orgChart.size)
                val iterator = orgChart.asIterable().iterator()
                assertEquals(Position.CEO, iterator.next().value.position)
                assertEquals(Position.CTO, iterator.next().value.position)
                assertEquals(Position.CPO, iterator.next().value.position)
                assertEquals(Position.EngineeringManager, iterator.next().value.position)
                assertEquals(Position.EngineeringManager, iterator.next().value.position)
            }
        }

    @Test
    fun `get org chart for employee with expand top`() =
        e2eTest {
            prepareDb(sql)
            val swe = EmployeeId.fromString("00000000-0000-0000-0000-000000000010")
            val client = configureClient()

            val response =
                client.get("/api/v1/hris/organization/org-chart") {
                    url {
                        parameters.append("employeeId", swe.toString())
                        parameters.append("expand", Expand.Top.name)
                        parameters.append("step", "1")
                    }
                    contentType(ContentType.Application.Json)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            response.body<Map<UUID, OrgChartEmployee>>().let { orgChart ->
                assertEquals(5, orgChart.size)
                val iterator = orgChart.asIterable().iterator()
                assertEquals(Position.CPO, iterator.next().value.position)
                assertEquals(Position.ProductManager, iterator.next().value.position)
                assertEquals(Position.ProductManager, iterator.next().value.position)
                assertEquals(Position.SoftwareEngineer, iterator.next().value.position)
                assertEquals(Position.SoftwareEngineer, iterator.next().value.position)
            }
        }

    @Test
    fun `ceo expands to the bottom max`() =
        e2eTest {
            prepareDb(sql)
            val swe = EmployeeId.fromString("00000000-0000-0000-0000-000000000001")
            val client = configureClient()

            val response =
                client.get("/api/v1/hris/organization/org-chart") {
                    url {
                        parameters.append("employeeId", swe.toString())
                        parameters.append("expand", Expand.Bottom.name)
                        parameters.append("step", "999")
                    }
                    contentType(ContentType.Application.Json)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            response.body<Map<UUID, OrgChartEmployee>>().let { orgChart ->
                assertEquals(11, orgChart.size)
                generateSequence(1) { it + 1 }.take(11).forEach { i ->
                    val append = i.toString().padStart(2, '0')
                    "00000000-0000-0000-0000-0000000000$append".let(UUID::fromString).also {
                        assertNotNull(orgChart[it])
                        assertNotNull(orgChart[it]?.name)
                        assertNotNull(orgChart[it]?.surname)
                        assertNotNull(orgChart[it]?.position)
                    }
                }
            }
        }

    @Test
    fun `org chart for unknown employee`() =
        e2eTest {
            val client = configureClient()

            val response =
                client.get("/api/v1/hris/organization/org-chart") {
                    url { parameters.append("employeeId", UUID.randomUUID().toString()) }
                    contentType(ContentType.Application.Json)
                }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }

    @Test
    fun `org chart for employee id in wrong format`() =
        e2eTest {
            val client = configureClient()

            val response =
                client.get("/api/v1/hris/organization/org-chart") {
                    url { parameters.append("employeeId", "unknownFormat") }
                    contentType(ContentType.Application.Json)
                }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
}
