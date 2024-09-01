package org.mycompany.hris.employee

import org.mycompany.hris.employee.model.CreateEmployeeRequest
import org.mycompany.hris.employee.model.CreateEmployeeResponse
import org.mycompany.hris.employee.model.GetEmployeeResponse
import org.mycompany.hris.employee.model.PatchEmployeeRequest
import org.mycompany.hris.exception.BadRequestException
import org.mycompany.hris.exception.NotFoundException
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.utils.inTx
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

class EmployeeService(
    private val employeeRepository: EmployeeRepository,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    suspend fun createEmployee(request: CreateEmployeeRequest): CreateEmployeeResponse =
        inTx {
            checkCreateEmployeeData(request)
            val employeeId = EmployeeId(UUID.randomUUID())
            employeeRepository.createEmployee(employeeId, request)
            request.subordinates?.let { employeeRepository.updateSupervisor(employeeId, it) }
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
            checkUpdateEmployeeData(employeeId, request)
            employeeRepository.updateEmployees(listOf(employeeId), request)
            request.subordinates?.let { employeeRepository.updateSupervisor(employeeId, it) }
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
            val supervisorId = employeeRepository.deleteEmployeeById(employeeId)
            employeeRepository.updateSupervisor(employeeId, supervisorId)
        }

    internal suspend fun isEmployeeExist(employeeId: EmployeeId) = employeeRepository.isEmployeeExist(employeeId)

    private suspend fun checkCreateEmployeeData(request: CreateEmployeeRequest) {
        val supervisor = request.supervisor
        val subordinates = request.subordinates.orEmpty()
        supervisor?.let {
            val supervisorList = employeeRepository.getEmployeeById(it)
            if (supervisorList.isEmpty()) {
                throw BadRequestException("Employee (supervisor) with id $supervisor doesn't exist")
            }
            val position = request.position
            val supervisor = supervisorList.first()
            if (position.order < supervisor.position.order) {
                throw BadRequestException("Supervisor has a lower position (${supervisor.position}) then employee $position")
            }
            if (subordinates.contains(it)) {
                throw BadRequestException("Supervisor can't be one of subordinates")
            }
        }
        subordinates.forEach {
            if (!employeeRepository.isEmployeeExist(it)) {
                throw BadRequestException("Employee (subordinate) with id $it doesn't exist")
            }
        }
    }

    private suspend fun checkUpdateEmployeeData(
        employeeId: EmployeeId,
        request: PatchEmployeeRequest,
    ) {
        val employeeList = employeeRepository.getEmployeeById(employeeId)
        if (employeeList.isEmpty()) {
            throw BadRequestException("Employee with id $employeeId doesn't exist")
        }
        val subordinates = request.subordinates.orEmpty()
        request.supervisor?.let {
            if (!employeeRepository.isEmployeeExist(it)) {
                throw BadRequestException("Employee (supervisor) with id $it doesn't exist")
            }
            if (subordinates.contains(it)) {
                throw BadRequestException("Supervisor can't be one of subordinates")
            }
        }
        request.position?.let { position ->
            if (request.supervisor == null) {
                employeeList.first().supervisor
            } else {
                request.supervisor
            }?.let { supervisorId ->
                val supervisorList = employeeRepository.getEmployeeById(supervisorId)
                if (supervisorList.isEmpty()) {
                    logger.warn("Employee (supervisor) with id $supervisorId doesn't exist. Skip supervisor check")
                    return@let
                }
                val supervisor = supervisorList.first()
                if (position.order < supervisor.position.order) {
                    throw BadRequestException("Supervisor has a lower position (${supervisor.position}) then employee $position")
                }
            }
        }
        subordinates.forEach {
            if (!employeeRepository.isEmployeeExist(it)) {
                throw BadRequestException("Employee (subordinate) with id $it doesn't exist")
            }
        }
    }
}
