package org.mycompany.hris.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class ReviewScore
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(
        @JsonValue val value: Short,
    ) {
        init {
            assert(value in 0..10)
        }
    }
