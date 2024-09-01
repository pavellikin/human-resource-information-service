package org.mycompany.hris.orgchart.model

import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.Name
import org.mycompany.hris.model.Position
import org.mycompany.hris.model.Surname

// For the org chart view it should be enough only name, surname and position.
// However, without employeeId FE will not be able to request employee details to expand the org chart node.
// Without supervisor and subordinates FE will not be able to draw an org chart tree.
data class OrgChartEmployee(
    val employeeId: EmployeeId,
    val name: Name,
    val surname: Surname,
    val position: Position,
    val supervisor: EmployeeId? = null,
)
