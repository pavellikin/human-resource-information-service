package org.mycompany.hris.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import java.util.UUID

data class EmployeeId
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(
        @JsonValue val value: UUID,
    ) {
        companion object {
            fun fromString(employeeId: String) = EmployeeId(UUID.fromString(employeeId))
        }
    }
