package org.mycompany.hris.performancereview

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import org.mycompany.hris.configuration.tables.PerformanceReviewsTable
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.ReviewComment
import org.mycompany.hris.model.ReviewScore
import org.mycompany.hris.performancereview.model.EmployeePerformanceReview
import org.mycompany.hris.performancereview.model.SubmitPerformanceReviewRequest

class PerformanceReviewRepository {
    suspend fun upsertPerformanceReview(request: SubmitPerformanceReviewRequest) =
        withContext(Dispatchers.IO) {
            with(PerformanceReviewsTable) {
                upsert { statement ->
                    statement[reviewee] = request.revieweeId.value
                    statement[reviewer] = request.reviewerId.value
                    statement[comment] = request.comment?.value
                    statement[performanceScore] = request.performance.value
                    statement[softSkillsScore] = request.softSkills.value
                    statement[independenceScore] = request.independence.value
                    statement[aspirationForGrowthScore] = request.aspirationForGrowth.value
                    statement[createdAt] = request.date
                }
            }
        }

    suspend fun getPerformanceReviewsForEmployee(
        employeeId: EmployeeId,
        limit: Int,
        offset: Long,
    ) = withContext(Dispatchers.IO) {
        with(PerformanceReviewsTable) {
            selectAll().where(reviewee eq employeeId.value)
                .orderBy(createdAt)
                .limit(limit, offset)
                .map { statement ->
                    EmployeePerformanceReview(
                        revieweeId = EmployeeId(statement[reviewee]),
                        performance = ReviewScore(statement[performanceScore]),
                        softSkills = ReviewScore(statement[softSkillsScore]),
                        independence = ReviewScore(statement[independenceScore]),
                        aspirationForGrowth = ReviewScore(statement[aspirationForGrowthScore]),
                        comment = statement[comment]?.let(::ReviewComment),
                        date = statement[createdAt],
                    )
                }
        }
    }
}
