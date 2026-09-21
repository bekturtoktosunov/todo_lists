package com.tradebyte.challenge.todo_list.model.dto

import java.time.Instant
import java.util.UUID

data class TodoResponse(
    val id: UUID,
    val description: String,
    val status: TodoStatusResponse,
    val creationDateTime: Instant,
    val dueDateTime: Instant,
    val doneDateTime: Instant?
)