package org.mycompany.hris.employee.model

import org.mycompany.hris.model.Email
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.Name
import org.mycompany.hris.model.Position
import org.mycompany.hris.model.Surname

data class GetEmployeeResponse(
    val employeeId: EmployeeId,
    val name: Name,
    val surname: Surname,
    val email: Email,
    val position: Position,
    val supervisor: EmployeeId? = null,
    val subordinates: List<EmployeeId>? = null,
)
