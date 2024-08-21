package org.mycompany.hris.orgchart

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.mycompany.hris.configuration.tables.EmployeesTable
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
                    .map { statement ->
                        OrgChartEmployee(
                            employeeId = EmployeeId(statement[id]),
                            name = Name(statement[name]),
                            surname = Surname(statement[surname]),
                            position = Position.valueOf(statement[position]),
                            supervisor = statement[supervisor]?.let { EmployeeId(it) },
                            subordinates = statement[subordinates]?.let { s -> s.map { EmployeeId(it) } },
                        )
                    }
            }
        }

    suspend fun getFor(employeeIds: List<EmployeeId>) =
        withContext(Dispatchers.IO) {
            with(EmployeesTable) {
                select(id, name, surname, position, supervisor, subordinates)
                    .where(id inList employeeIds.map { it.value })
                    .map { statement ->
                        OrgChartEmployee(
                            employeeId = EmployeeId(statement[id]),
                            name = Name(statement[name]),
                            surname = Surname(statement[surname]),
                            position = Position.valueOf(statement[position]),
                            supervisor = statement[supervisor]?.let { EmployeeId(it) },
                            subordinates = statement[subordinates]?.let { s -> s.map { EmployeeId(it) } },
                        )
                    }
            }
        }
}
