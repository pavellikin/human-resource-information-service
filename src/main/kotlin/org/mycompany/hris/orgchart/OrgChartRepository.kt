package org.mycompany.hris.orgchart

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.selectAll
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

    suspend fun getWithColleagues(employeeId: EmployeeId) =
        withContext(Dispatchers.IO) {
            val above = EmployeesTable.alias("above")
            val below = EmployeesTable.alias("below")
            val current = EmployeesTable.alias("current")
            current
                .join(above, JoinType.LEFT, above[id], current[supervisor])
                .join(below, JoinType.LEFT, below[supervisor], current[id])
                .selectAll()
                .where(
                    current[id] eq employeeId.value,
                ).flatMap { row ->
                    listOf(above, current, below).mapNotNull { alias ->
                        val id = row[alias[id]] ?: return@mapNotNull null
                        OrgChartEmployee(
                            employeeId = EmployeeId(id),
                            name = Name(row[alias[name]]),
                            surname = Surname(row[alias[surname]]),
                            position = Position.valueOf(row[alias[position]]),
                            supervisor = row[alias[supervisor]]?.let { EmployeeId(it) },
                            subordinates = row[alias[subordinates]]?.let { s -> s.map { EmployeeId(it) } },
                        )
                    }
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
