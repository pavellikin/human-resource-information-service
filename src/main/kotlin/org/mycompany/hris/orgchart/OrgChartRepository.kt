package org.mycompany.hris.orgchart

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.mycompany.hris.configuration.tables.EmployeesTable
import org.mycompany.hris.configuration.tables.EmployeesTable.id
import org.mycompany.hris.configuration.tables.EmployeesTable.name
import org.mycompany.hris.configuration.tables.EmployeesTable.position
import org.mycompany.hris.configuration.tables.EmployeesTable.subordinates
import org.mycompany.hris.configuration.tables.EmployeesTable.supervisor
import org.mycompany.hris.configuration.tables.EmployeesTable.surname
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.Name
import org.mycompany.hris.model.Position
import org.mycompany.hris.model.Surname
import org.mycompany.hris.orgchart.model.OrgChartEmployee

class OrgChartRepository {
    suspend fun getAllEmployees() =
        withContext(Dispatchers.IO) {
            with(EmployeesTable) {
                select(id, name, surname, position, supervisor, subordinates)
                    .map { it.toEmployee() }
            }
        }

    suspend fun getEmployees(employeeIds: List<EmployeeId>) =
        withContext(Dispatchers.IO) {
            with(EmployeesTable) {
                select(id, name, surname, position, supervisor, subordinates)
                    .where(id inList employeeIds.map { it.value })
                    .map { it.toEmployee() }
            }
        }

    private fun ResultRow.toEmployee() =
        OrgChartEmployee(
            employeeId = EmployeeId(this[id]),
            name = Name(this[name]),
            surname = Surname(this[surname]),
            position = Position.valueOf(this[position]),
            supervisor = this[supervisor]?.let { EmployeeId(it) },
            subordinates = this[subordinates]?.let { s -> s.map { EmployeeId(it) } },
        )
}
