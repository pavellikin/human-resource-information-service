package org.mycompany.hris.orgchart

import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.runBlocking
import org.mycompany.hris.exception.NotFoundException
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.orgchart.model.Expand
import org.mycompany.hris.orgchart.model.OrgChartEmployee
import org.mycompany.hris.utils.inTx

class OrgChartService(
    private val orgChartRepository: OrgChartRepository,
    orgChartConfig: OrgChartConfig,
) {
    private val cache =
        Caffeine.newBuilder()
            .maximumSize(orgChartConfig.cache.maxSize)
            .expireAfterWrite(orgChartConfig.cache.expireTime)
            .refreshAfterWrite(orgChartConfig.cache.expireTime)
            .recordStats()
            .build<Unit, Map<EmployeeId, OrgChartEmployee>> {
                runBlocking { internalGetAllOrgChart() }
            }

    // Heavy operation.
    // Let's assume the median name length is 10 symbols and median surname length is 20. Every employee has 1 supervisor and 5 subordinates.
    // The row size without email will be 16 + 10 + 20 + 40 + 16 + (16 * 5) = 182 bytes. Let's use 200 bytes for simplicity.
    // For a medium size organization of ~1000 employees the amount of data to extract will be 200 * 1000 = 200 Kb.
    // For a large organization of 2 million employees (Walmart) the data to extract will be ~400 Mb.
    // To reduce IO we can afford cashing of org structure in memory for small organization.
    // For big organizations this cache should be moved to a distributed cache (Redis) or replaced with SQL queries.
    suspend fun getAllOrgChart(): Map<EmployeeId, OrgChartEmployee> {
        return cache.asMap().getOrPut(Unit) { internalGetAllOrgChart() }
    }

    suspend fun getEmployeeOrgChart(
        employeeId: EmployeeId,
        expand: Expand,
        step: Int,
    ): Map<EmployeeId, OrgChartEmployee> {
        val orgChart = getAllOrgChart()
        val employee = orgChart[employeeId] ?: throw NotFoundException("Employee $employeeId not found")
        val deque = ArrayDeque<OrgChartEmployee>()
        deque.add(employee)
        employee.supervisor?.let { supervisor -> orgChart[supervisor]?.let { oce -> deque.add(oce) } }
        employee.subordinates?.let { subordinates -> subordinates.forEach { s -> orgChart[s]?.let { oce -> deque.add(oce) } } }
        when (expand) {
            Expand.Top -> {
                var counter = 0
                var supervisor = employee.supervisor
                while (supervisor != null && counter < step) {
                    orgChart[supervisor]?.let {
                        supervisor = it.supervisor
                        supervisor?.let { s ->
                            orgChart[s]?.let { oce ->
                                deque.add(oce)
                                oce.subordinates?.forEach { s -> orgChart[s]?.let { oce -> deque.add(oce) } }
                            }
                        }
                    }
                    counter++
                }
            }

            Expand.Bottom -> {
                var counter = 0
                var subordinates = employee.subordinates
                while (subordinates != null && counter < step) {
                    val nextSubordinates = mutableListOf<EmployeeId>()
                    subordinates.forEach { subordinate ->
                        orgChart[subordinate]?.let {
                            it.subordinates?.let { s -> nextSubordinates.addAll(s) }
                            it.subordinates?.forEach { s -> orgChart[s]?.let { oce -> deque.add(oce) } }
                        }
                    }
                    subordinates = nextSubordinates
                    counter++
                }
            }

            Expand.None -> {}
        }
        return deque
            .sortedBy { it.position.order }
            .associateBy { it.employeeId }
    }

    private suspend fun internalGetAllOrgChart(): Map<EmployeeId, OrgChartEmployee> {
        return inTx { orgChartRepository.getAllEmployees() }
            .sortedBy { it.position.order }
            .associateBy { it.employeeId }
    }
}
