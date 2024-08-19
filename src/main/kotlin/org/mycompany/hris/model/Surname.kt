package org.mycompany.hris.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class Surname
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(
        @JsonValue val value: String,
    ) {
        init {
            assert(value.length <= 50)
        }
    }
