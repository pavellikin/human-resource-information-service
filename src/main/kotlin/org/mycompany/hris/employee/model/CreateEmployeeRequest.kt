package org.mycompany.hris.employee.model

import org.mycompany.hris.model.Email
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.Name
import org.mycompany.hris.model.Position
import org.mycompany.hris.model.Surname

data class CreateEmployeeRequest(
    val name: Name,
    val surname: Surname,
    val email: Email,
    val position: Position,
    // I see several use cases where employee can not have no supervisor:
    // 1. CEO/CTO
    // 2. The new employer was created as a placeholder for a new position in a team
    val supervisor: EmployeeId? = null,
    // Subordinates can be empty or null for the developers in the organization
    val subordinates: Set<EmployeeId>? = null,
)
