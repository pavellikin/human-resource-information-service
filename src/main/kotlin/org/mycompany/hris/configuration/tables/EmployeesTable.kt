package org.mycompany.hris.configuration.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import java.time.LocalDate
import java.util.UUID

object EmployeesTable : Table() {
    val id = uuid("id")
    val name = varchar("name", 50)
    val surname = varchar("surname", 50)
    val email = varchar("email", 60)

    // In general, it is good to have a dedicated table to store all available positions inside the company.
    // The positions table can be very simple - id, name.
    // For simplicity, I will omit this table and will store positions names normalized by the service itself.
    val position = varchar("position", 50)
    val supervisor = uuid("supervisor").nullable()
    val subordinates = array<UUID>("subordinates").nullable()
    val createdAt = date("created_at").default(LocalDate.now())

    override val primaryKey: PrimaryKey = PrimaryKey(id)
}