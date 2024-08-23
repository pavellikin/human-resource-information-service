package org.mycompany.hris.performancereview

import org.mycompany.hris.employee.EmployeeService
import org.mycompany.hris.exception.NotFoundException
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.performancereview.model.PerformanceReviewHistoryResponse
import org.mycompany.hris.performancereview.model.SubmitPerformanceReviewRequest
import org.mycompany.hris.utils.inTx

class PerformanceReviewService(
    private val employeeService: EmployeeService,
    private val performanceReviewRepository: PerformanceReviewRepository,
) {
    // The size of one row can be 16 + 16 + 2 + 2 + 2 + 2 + 500 (median comment) + 4 = ~550 byte.
    // For the large organization of 2 million employees it will be 2 000 000 * 550 / 1024 / 1024 / 1024 = 1 Gb data per review cycle.
    suspend fun upsertPerformanceReview(request: SubmitPerformanceReviewRequest) {
        inTx {
            if (!employeeService.isEmployeeExist(request.revieweeId)) {
                throw NotFoundException("Reviewee ${request.revieweeId} doesn't exist")
            }
            if (!employeeService.isEmployeeExist(request.reviewerId)) {
                throw NotFoundException("Reviewer ${request.reviewerId} doesn't exist")
            }
            performanceReviewRepository.upsertPerformanceReview(request)
        }
    }

    suspend fun getPerformanceReviews(
        employeeId: EmployeeId,
        limit: Int,
        offset: Long,
    ): PerformanceReviewHistoryResponse {
        val reviews = inTx { performanceReviewRepository.getPerformanceReviewsForEmployee(employeeId, limit, offset) }
        return PerformanceReviewHistoryResponse(reviews, limit, offset)
    }
}
