package org.mycompany.hris.performancereview

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.Test
import org.mycompany.hris.AbstractE2eTest
import org.mycompany.hris.configuration.tables.EmployeesTable
import org.mycompany.hris.configuration.tables.PerformanceReviewsTable
import org.mycompany.hris.givenEmail
import org.mycompany.hris.givenEmployeeId
import org.mycompany.hris.givenName
import org.mycompany.hris.givenPosition
import org.mycompany.hris.givenReviewComment
import org.mycompany.hris.givenReviewScore
import org.mycompany.hris.givenSurname
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.ReviewComment
import org.mycompany.hris.performancereview.model.PerformanceReviewHistoryResponse
import org.mycompany.hris.performancereview.model.SubmitPerformanceReviewRequest
import java.time.LocalDate
import kotlin.test.assertEquals

class PerformanceReviewServiceE2ETest : AbstractE2eTest() {
    @Test
    fun `create review`() =
        e2eTest {
            val client = configureClient()
            val request =
                givenSubmitPerformanceReviewRequest().also {
                    createEmployee(it.revieweeId)
                    createEmployee(it.reviewerId)
                }

            val response =
                client.put("/api/v1/hris/performance-reviews") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            newSuspendedTransaction {
                with(PerformanceReviewsTable) {
                    selectAll().where(reviewee eq request.revieweeId.value).toList().also { dbReview ->
                        assertEquals(1, dbReview.size)
                        assertEquals(request.revieweeId.value, dbReview.first()[reviewee])
                        assertEquals(request.reviewerId.value, dbReview.first()[reviewer])
                        assertEquals(request.comment?.value, dbReview.first()[comment])
                        assertEquals(request.performance.value, dbReview.first()[performanceScore])
                        assertEquals(request.softSkills.value, dbReview.first()[softSkillsScore])
                        assertEquals(request.independence.value, dbReview.first()[independenceScore])
                        assertEquals(request.aspirationForGrowth.value, dbReview.first()[aspirationForGrowthScore])
                        assertEquals(request.date, dbReview.first()[createdAt])
                    }
                }
            }
        }

    @Test
    fun `update review`() =
        e2eTest {
            val client = configureClient()
            val request =
                givenSubmitPerformanceReviewRequest().also {
                    createEmployee(it.revieweeId)
                    createEmployee(it.reviewerId)
                }
            val newComment = ReviewComment("New review comment")

            client.put("/api/v1/hris/performance-reviews") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val response =
                client.put("/api/v1/hris/performance-reviews") {
                    contentType(ContentType.Application.Json)
                    setBody(request.copy(comment = newComment))
                }

            assertEquals(HttpStatusCode.OK, response.status)
            newSuspendedTransaction {
                with(PerformanceReviewsTable) {
                    selectAll().where(reviewee eq request.revieweeId.value).toList().also { dbReview ->
                        assertEquals(1, dbReview.size)
                        // new comment
                        assertEquals(newComment.value, dbReview.first()[comment])
                        // all other fields stays untouched
                        assertEquals(request.revieweeId.value, dbReview.first()[reviewee])
                        assertEquals(request.reviewerId.value, dbReview.first()[reviewer])
                        assertEquals(request.performance.value, dbReview.first()[performanceScore])
                        assertEquals(request.softSkills.value, dbReview.first()[softSkillsScore])
                        assertEquals(request.independence.value, dbReview.first()[independenceScore])
                        assertEquals(request.aspirationForGrowth.value, dbReview.first()[aspirationForGrowthScore])
                        assertEquals(request.date, dbReview.first()[createdAt])
                    }
                }
            }
        }

    @Test
    fun `get reviews`() =
        e2eTest {
            val client = configureClient()
            val request =
                givenSubmitPerformanceReviewRequest().also {
                    createEmployee(it.revieweeId)
                    createEmployee(it.reviewerId)
                }
            val previousReviewDate = request.date.minusDays(1)

            client.put("/api/v1/hris/performance-reviews") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            client.put("/api/v1/hris/performance-reviews") {
                contentType(ContentType.Application.Json)
                setBody(request.copy(date = previousReviewDate))
            }
            val response =
                client.get("/api/v1/hris/performance-reviews/${request.revieweeId}") {
                    url {
                        parameters.append("limit", "1")
                        parameters.append("offset", "0")
                    }
                    contentType(ContentType.Application.Json)
                }

            assertEquals(HttpStatusCode.OK, response.status)
            response.body<PerformanceReviewHistoryResponse>().let {
                assertEquals(1, it.limit)
                assertEquals(0, it.offset)
                assertEquals(1, it.reviews.size)
                assertEquals(previousReviewDate, it.reviews.first().date)
                assertEquals(request.revieweeId, it.reviews.first().revieweeId)
                assertEquals(request.comment, it.reviews.first().comment)
                assertEquals(request.performance, it.reviews.first().performance)
                assertEquals(request.softSkills, it.reviews.first().softSkills)
                assertEquals(request.independence, it.reviews.first().independence)
                assertEquals(request.aspirationForGrowth, it.reviews.first().aspirationForGrowth)
            }
        }

    private fun givenSubmitPerformanceReviewRequest(revieweeId: EmployeeId = givenEmployeeId()) =
        SubmitPerformanceReviewRequest(
            revieweeId,
            givenEmployeeId(),
            performance = givenReviewScore(),
            softSkills = givenReviewScore(),
            independence = givenReviewScore(),
            aspirationForGrowth = givenReviewScore(),
            comment = givenReviewComment(),
            date = LocalDate.now(),
        )

    private suspend fun createEmployee(employeeId: EmployeeId) =
        newSuspendedTransaction {
            EmployeesTable.insert { statement ->
                statement[id] = employeeId.value
                statement[name] = givenName().value
                statement[surname] = givenSurname().value
                statement[email] = givenEmail().value
                statement[position] = givenPosition().name
                statement[supervisor] = givenEmployeeId().value
            }
        }
}
