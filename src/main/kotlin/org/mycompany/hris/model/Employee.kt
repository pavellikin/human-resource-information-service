package org.mycompany.hris.model

data class Employee(
    val employeeId: EmployeeId,
    val name: Name,
    val surname: Surname,
    val email: Email,
    val position: Position,
    val supervisor: EmployeeId? = null,
    val subordinates: List<EmployeeId>? = null,
    val numberOfSubordinates: Int = subordinates?.size ?: 0,
)
