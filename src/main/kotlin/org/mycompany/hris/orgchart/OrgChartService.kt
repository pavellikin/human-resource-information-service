package org.mycompany.hris.orgchart

import org.mycompany.hris.exception.NotFoundException
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.orgchart.model.Expand
import org.mycompany.hris.orgchart.model.OrgChartEmployee
import org.mycompany.hris.utils.inTx

class OrgChartService(
    private val orgChartRepository: OrgChartRepository,
) {
    // Heavy operation.
    // Let's assume the median name length is 10 symbols and median surname length is 20. Every employee has 1 supervisor and 5 subordinates.
    // The row size without email will be 16 + 10 + 20 + 40 + 16 + (16 * 5) = 182 bytes. Let's use 200 bytes for simplicity.
    // For a medium size organization of ~1000 employees the amount of data to extract will be 200 * 1000 = 200 Kb.
    // For a large organization of 2 million employees (Walmart) the data to extract will be ~400 Mb.
    // large big organizations it makes sense to add a distributed cache.
    suspend fun getAllOrgChart(): Map<EmployeeId, OrgChartEmployee> {
        return inTx { orgChartRepository.getAllEmployees() }
            .sortedBy { it.position.order }
            .associateBy { it.employeeId }
    }

    suspend fun getEmployeeOrgChart(
        employeeId: EmployeeId,
        expand: Expand,
        step: Int,
    ): Map<EmployeeId, OrgChartEmployee> =
        inTx {
            val employeeTree = orgChartRepository.getWithColleagues(employeeId).associateBy { it.employeeId }
            if (employeeTree.isEmpty()) {
                throw NotFoundException("Employee $employeeId not found")
            }
            val subChart = employeeTree.toMutableMap()
            val employee = checkNotNull(employeeTree[employeeId])
            when (expand) {
                Expand.None -> {}
                Expand.Top -> expandTop(subChart, employee, step)
                Expand.Bottom -> expandBottom(subChart, employee, step)
            }
            return@inTx subChart.values
                .sortedBy { it.position.order }
                .associateBy { it.employeeId }
        }

    private suspend fun expandTop(
        subChart: MutableMap<EmployeeId, OrgChartEmployee>,
        employee: OrgChartEmployee,
        step: Int,
    ) {
        var counter = 0
        var supervisor = employee.supervisor
        while (supervisor != null && counter < step) {
            subChart[supervisor]?.let { e ->
                supervisor?.let { s -> orgChartRepository.getWithColleagues(s).onEach { subChart[it.employeeId] = it } }
                supervisor = e.supervisor
            }
            counter++
        }
    }

    private suspend fun expandBottom(
        subChart: MutableMap<EmployeeId, OrgChartEmployee>,
        employee: OrgChartEmployee,
        step: Int,
    ) {
        var counter = 0
        var subordinates = employee.subordinates
        while (!subordinates.isNullOrEmpty() && counter < step) {
            val oces = orgChartRepository.getEmployees(subordinates).onEach { subChart[it.employeeId] = it }
            subordinates = oces.flatMap { it.subordinates.orEmpty() }
            counter++
        }
    }
}
