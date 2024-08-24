package org.mycompany.hris

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OperationalTest : AbstractE2eTest() {
    @Test
    fun `get metrics`() =
        e2eTest {
            val client = configureClient()

            val response = client.get("/metrics")

            assertEquals(HttpStatusCode.OK, response.status)
        }

    @Test
    fun `get health`() =
        e2eTest {
            val client = configureClient()

            val response = client.get("/health")

            assertEquals(HttpStatusCode.OK, response.status)
        }
}
