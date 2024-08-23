package org.mycompany.hris.performancereview.model

import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.ReviewComment
import org.mycompany.hris.model.ReviewScore
import java.time.LocalDate

// Reviewee doesn't see who filled the review.
data class EmployeePerformanceReview(
    val revieweeId: EmployeeId,
    val performance: ReviewScore,
    val softSkills: ReviewScore,
    val independence: ReviewScore,
    val aspirationForGrowth: ReviewScore,
    val comment: ReviewComment?,
    val date: LocalDate,
)
