package org.mycompany.hris.employee.model

import com.fasterxml.jackson.annotation.JsonIgnore
import org.mycompany.hris.model.EmployeeId
import org.mycompany.hris.model.Position

// Cases where employee changes name or surname are omitted.
data class PatchEmployeeRequest(
    val position: Position? = null,
    val supervisor: EmployeeId? = null,
    // For simplicity, it is only possible to rewrite all subordinates.
    val subordinates: Set<EmployeeId>? = null,
) {
    @JsonIgnore
    fun isEmpty() = position == null && supervisor == null && subordinates == null
}
