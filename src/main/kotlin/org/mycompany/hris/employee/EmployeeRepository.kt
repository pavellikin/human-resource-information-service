package org.mycompany.hris.employee

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.deleteReturning
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.mycompany.hris.configuration.tables.EmployeesTable
import org.mycompany.hris.configuration.tables.EmployeesTable.email
import org.mycompany.hris.configuration.tables.EmployeesTable.name
import org.mycompany.hris.configuration.tables.EmployeesTable.position
import org.mycompany.hris.configuration.tables.EmployeesTable.subordinates
import org.mycompany.hris.configuration.tables.EmployeesTable.supervisor
import org.mycompany.hris.configuration.tables.EmployeesTable.surname
import org.mycompany.hris.employee.model.CreateEmployeeRequest
import org.mycompany.hris.employee.model.GetEmployeeResponse
import org.mycompany.hris.employee.model.PatchEmployeeRequest
import org.mycompany.hris.model.Email
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.Name
import org.mycompany.hris.model.Position
import org.mycompany.hris.model.Surname

class EmployeeRepository {
    suspend fun createEmployee(
        employeeId: EmployeeId,
        request: CreateEmployeeRequest,
    ) = withContext(Dispatchers.IO) {
        EmployeesTable.insert { statement ->
            statement[id] = employeeId.value
            statement[name] = request.name.value
            statement[surname] = request.surname.value
            statement[email] = request.email.value
            statement[position] = request.position.name
            statement[supervisor] = request.supervisor?.value
            statement[subordinates] = request.subordinates?.map { s -> s.value }
        }
    }

    suspend fun updateEmployees(
        employeeIds: List<EmployeeId>,
        request: PatchEmployeeRequest,
    ) = withContext(Dispatchers.IO) {
        with(EmployeesTable) {
            update({ id inList employeeIds.map { it.value } }) { statement ->
                request.position?.let { statement[position] = it.name }
                request.supervisor?.let { statement[supervisor] = it.value }
                request.subordinates?.let { statement[subordinates] = it.map { s -> s.value } }
            }
        }
    }

    suspend fun getEmployeeById(employeeId: EmployeeId) =
        withContext(Dispatchers.IO) {
            EmployeesTable.selectAll().where(EmployeesTable.id eq employeeId.value)
                .map { statement ->
                    GetEmployeeResponse(
                        employeeId = employeeId,
                        name = Name(statement[name]),
                        surname = Surname(statement[surname]),
                        email = Email(statement[email]),
                        position = Position.valueOf(statement[position]),
                        supervisor = statement[supervisor]?.let { EmployeeId(it) },
                        subordinates = statement[subordinates]?.let { s -> s.map { EmployeeId(it) } },
                    )
                }
        }

    suspend fun deleteEmployeeById(employeeId: EmployeeId) =
        withContext(Dispatchers.IO) {
            with(EmployeesTable) {
                deleteReturning(listOf(supervisor, subordinates)) { id eq employeeId.value }
                    .first()
                    .let { statement ->
                        statement[supervisor]?.let { EmployeeId(it) } to statement[subordinates]?.let { s -> s.map { EmployeeId(it) } }
                    }
            }
        }

    suspend fun isEmployeeExist(employeeId: EmployeeId) =
        withContext(Dispatchers.IO) {
            with(EmployeesTable) {
                select(id.count()).where { id eq employeeId.value }.count() > 0
            }
        }
}
