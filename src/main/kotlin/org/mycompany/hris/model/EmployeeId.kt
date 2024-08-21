package org.mycompany.hris.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.mycompany.hris.exception.BadRequestException
import java.util.UUID

data class EmployeeId
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(
        @JsonValue val value: UUID,
    ) {
        companion object {
            fun fromString(employeeId: String) =
                try {
                    EmployeeId(UUID.fromString(employeeId))
                } catch (ex: IllegalArgumentException) {
                    throw BadRequestException(ex.message ?: "Invalid UUID string: $employeeId")
                }
        }

        override fun toString(): String = value.toString()
    }
