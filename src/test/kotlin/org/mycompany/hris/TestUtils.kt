package org.mycompany.hris

import org.mycompany.hris.model.Email
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.Name
import org.mycompany.hris.model.Position
import org.mycompany.hris.model.Surname
import java.util.UUID

fun givenName() = Name("Frodo")

fun givenSurname() = Surname("Baggins")

fun givenEmail() = Email("frodo.baggins@mycompany.com")

fun givenPosition() = Position.SoftwareEngineer

fun givenEmployeeId() = EmployeeId(UUID.randomUUID())
