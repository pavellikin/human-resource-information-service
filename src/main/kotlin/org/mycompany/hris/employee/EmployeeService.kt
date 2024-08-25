package org.mycompany.hris.employee

import org.mycompany.hris.employee.model.CreateEmployeeRequest
import org.mycompany.hris.employee.model.CreateEmployeeResponse
import org.mycompany.hris.employee.model.GetEmployeeResponse
import org.mycompany.hris.employee.model.PatchEmployeeRequest
import org.mycompany.hris.exception.BadRequestException
import org.mycompany.hris.exception.NotFoundException
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.Position
import org.mycompany.hris.utils.inTx
import java.util.UUID

class EmployeeService(
    private val employeeRepository: EmployeeRepository,
) {
    suspend fun createEmployee(request: CreateEmployeeRequest): CreateEmployeeResponse =
        inTx {
            checkEmployeeData(request.position, request.supervisor, request.subordinates)
            val employeeId = EmployeeId(UUID.randomUUID())
            employeeRepository.createEmployee(employeeId, request)
            CreateEmployeeResponse(employeeId)
        }

    suspend fun updateEmployee(
        employeeId: EmployeeId,
        request: PatchEmployeeRequest,
    ) {
        if (request.isEmpty()) {
            return
        }
        inTx {
            checkEmployeeData(request.position, request.supervisor, request.subordinates)
            employeeRepository.updateEmployees(listOf(employeeId), request)
        }
    }

    suspend fun getEmployee(employeeId: EmployeeId): GetEmployeeResponse {
        val employee = inTx { employeeRepository.getEmployeeById(employeeId) }
        if (employee.isEmpty()) {
            throw NotFoundException("Employee $employeeId not found")
        }
        return employee
            .map {
                GetEmployeeResponse(
                    employeeId = it.employeeId,
                    name = it.name,
                    surname = it.surname,
                    email = it.email,
                    position = it.position,
                    supervisor = it.supervisor,
                    numberOfSubordinates = it.numberOfSubordinates,
                )
            }
            .first()
    }

    suspend fun deleteEmployee(employeeId: EmployeeId) =
        inTx {
            val (supervisorId, subordinates) = employeeRepository.deleteEmployeeById(employeeId)
            val currentSubordinates = subordinates ?: emptyList()
            if (supervisorId != null) {
                val supervisor = employeeRepository.getEmployeeById(supervisorId)
                if (supervisor.isEmpty()) {
                    return@inTx
                }
                val newSubordinates = ((supervisor.first().subordinates ?: emptyList()) + currentSubordinates).toSet()
                if (newSubordinates.isNotEmpty()) {
                    employeeRepository.updateEmployees(listOf(supervisorId), PatchEmployeeRequest(subordinates = newSubordinates))
                }
            }
            employeeRepository.updateEmployees(currentSubordinates, PatchEmployeeRequest(supervisor = supervisorId))
        }

    internal suspend fun isEmployeeExist(employeeId: EmployeeId) = employeeRepository.isEmployeeExist(employeeId)

    private suspend fun checkEmployeeData(
        position: Position?,
        supervisorId: EmployeeId?,
        subordinates: Collection<EmployeeId>?,
    ) {
        supervisorId?.let {
            val supervisorList = employeeRepository.getEmployeeById(it)
            if (supervisorList.isEmpty()) {
                throw BadRequestException("Employee (supervisor) with id $supervisorId doesn't exist")
            }
            if (position != null) {
                val supervisor = supervisorList.first()
                if (position.order < supervisor.position.order) {
                    throw BadRequestException("Supervisor has a lower position (${supervisor.position}) then employee $position")
                }
            }
            if (setOf(it) == subordinates) {
                throw BadRequestException("Supervisor can't be one of subordinates")
            }
        }
        subordinates?.forEach {
            if (!employeeRepository.isEmployeeExist(it)) {
                throw BadRequestException("Employee (subordinate) with id $it doesn't exist")
            }
        }
    }
}
