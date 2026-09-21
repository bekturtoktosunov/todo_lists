package com.tradebyte.challenge.todo_list.model.dto

enum class TodoStatusResponse(val value: String) {
    NOT_DONE("not done"),
    DONE("done"),
    PAST_DUE("past due")
}