package com.tradebyte.challenge.todolist.model.dto.response

import com.fasterxml.jackson.annotation.JsonValue

enum class TodoStatusResponse(
    @get:JsonValue val value: String,
) {
    NOT_DONE("not done"),
    DONE("done"),
    PAST_DUE("past due"),
}
