package org.mycompany.hris.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.mycompany.hris.exception.BadRequestException

data class Email
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(
        @JsonValue val value: String,
    ) {
        init {
            val minValue = 15
            val maxValue = 60
            if (value.length < minValue || value.length > maxValue) {
                throw BadRequestException("Email size should be between $minValue and $maxValue symbols")
            }
        }
    }
