package org.mycompany.hris.performancereview.model

import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.ReviewComment
import org.mycompany.hris.model.ReviewScore
import java.time.LocalDate

data class SubmitPerformanceReviewRequest(
    val revieweeId: EmployeeId,
    val reviewerId: EmployeeId,
    val performance: ReviewScore,
    val softSkills: ReviewScore,
    val independence: ReviewScore,
    val aspirationForGrowth: ReviewScore,
    val comment: ReviewComment?,
    val date: LocalDate,
) {
    init {
        assert(revieweeId != reviewerId)
    }
}
