package com.tradebyte.challenge.todo_list.model.domain

import java.util.UUID
import java.time.Instant

data class TodoItem(
    val id: UUID = UUID.randomUUID(),
    val description: String,
    val status: TodoItemStatus = TodoItemStatus.NOT_DONE,
    val creationDateTime: Instant,
    val dueDateTime: Instant,
    val doneDateTime: Instant? = null
)