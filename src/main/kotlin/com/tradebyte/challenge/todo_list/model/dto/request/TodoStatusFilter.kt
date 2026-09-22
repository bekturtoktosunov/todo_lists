package com.tradebyte.challenge.todo_list.model.dto.request

import com.fasterxml.jackson.annotation.JsonValue

enum class TodoStatusFilter(@get:JsonValue val value: String) {
    NOT_DONE("not done"),
    DONE("done"),
    PAST_DUE("past due")
}