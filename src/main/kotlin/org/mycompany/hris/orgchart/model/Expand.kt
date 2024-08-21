package org.mycompany.hris.orgchart.model

import org.mycompany.hris.exception.BadRequestException

enum class Expand {
    Top,
    Bottom,
    None,
    ;

    companion object {
        fun fromString(value: String) =
            try {
                valueOf(value)
            } catch (ex: IllegalArgumentException) {
                throw BadRequestException(ex.message ?: "Invalid value: $value")
            }
    }
}
