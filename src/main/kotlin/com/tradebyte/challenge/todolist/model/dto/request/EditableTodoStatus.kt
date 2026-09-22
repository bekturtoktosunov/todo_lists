package com.tradebyte.challenge.todolist.model.dto.request

import com.fasterxml.jackson.annotation.JsonValue

enum class EditableTodoStatus(
    @get:JsonValue val value: String,
) {
    DONE("done"),
    NOT_DONE("not done"),
}
