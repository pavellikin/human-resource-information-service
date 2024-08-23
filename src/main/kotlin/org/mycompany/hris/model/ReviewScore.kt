package org.mycompany.hris.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.mycompany.hris.exception.BadRequestException

data class ReviewScore
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(
        @JsonValue val value: Short,
    ) {
        init {
            val minValue = 0
            val maxValue = 10
            if (value < minValue || value > maxValue) {
                throw BadRequestException("Review score should be between $minValue and $maxValue")
            }
        }
    }
