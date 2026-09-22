package com.tradebyte.challenge.todolist.model.domain

import java.time.Instant
import java.util.UUID

data class TodoItem(
    val id: UUID = UUID.randomUUID(),
    val description: String,
    val status: TodoItemStatus = TodoItemStatus.NOT_DONE,
    val creationDateTime: Instant,
    val dueDateTime: Instant,
    val doneDateTime: Instant? = null,
)
