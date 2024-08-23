package org.mycompany.hris.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.mycompany.hris.exception.BadRequestException

data class Name
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(
        @JsonValue val value: String,
    ) {
        init {
            val minValue = 1
            val maxValue = 50
            if (value.length < minValue || value.length > maxValue) {
                throw BadRequestException("Name size should be between $minValue and $maxValue symbols")
            }
        }
    }
