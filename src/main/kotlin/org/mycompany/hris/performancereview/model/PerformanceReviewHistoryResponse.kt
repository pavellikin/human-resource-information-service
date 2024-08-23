package org.mycompany.hris.performancereview.model

data class PerformanceReviewHistoryResponse(
    val reviews: List<EmployeePerformanceReview>,
    val limit: Int,
    val offset: Long,
)
