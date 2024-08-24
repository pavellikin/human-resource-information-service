package org.mycompany.hris.employee

import org.mycompany.hris.employee.model.CreateEmployeeRequest
import org.mycompany.hris.employee.model.CreateEmployeeResponse
import org.mycompany.hris.employee.model.GetEmployeeResponse
import org.mycompany.hris.employee.model.PatchEmployeeRequest
import org.mycompany.hris.exception.NotFoundException
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.utils.inTx
import java.util.UUID

class EmployeeService(
    private val employeeRepository: EmployeeRepository,
) {
    // Check subordinates
    // Check supervisor
    // Check if supervisor is higher in hierarchy
    suspend fun createEmployee(request: CreateEmployeeRequest): CreateEmployeeResponse {
        val employeeId = EmployeeId(UUID.randomUUID())
        inTx { employeeRepository.createEmployee(employeeId, request) }
        return CreateEmployeeResponse(employeeId)
    }

    suspend fun updateEmployee(
        employeeId: EmployeeId,
        request: PatchEmployeeRequest,
    ) {
        if (request.isEmpty()) {
            return
        }
        inTx { employeeRepository.updateEmployee(employeeId, request) }
    }

    suspend fun getEmployee(employeeId: EmployeeId): GetEmployeeResponse {
        val employee = inTx { employeeRepository.getEmployeeById(employeeId) }
        if (employee.isEmpty()) {
            throw NotFoundException("Employee $employeeId not found")
        }
        return employee.first()
    }

    suspend fun deleteEmployee(employeeId: EmployeeId) {
        inTx { employeeRepository.deleteEmployeeById(employeeId) }
    }

    internal suspend fun isEmployeeExist(employeeId: EmployeeId) = employeeRepository.isEmployeeExist(employeeId)
}
