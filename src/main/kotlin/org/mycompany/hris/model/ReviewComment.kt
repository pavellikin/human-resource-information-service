package org.mycompany.hris.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.mycompany.hris.exception.BadRequestException

data class ReviewComment
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(
        @JsonValue val value: String,
    ) {
        init {
            val maxValue = 2000
            if (value.length > maxValue) {
                throw BadRequestException("Review comment size max value is $maxValue")
            }
        }
    }
