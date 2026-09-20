package com.tradebyte.challenge.todo_list.model.domain

import kotlin.time.Clock
import kotlin.time.Instant

data class TodoItem(
    val description: String,
    val status: TodoItemStatus,
    val creationDateTime: Instant = Clock.System.now(),
    val dueDateTime: Instant,
    val doneDateTime: Instant? = null,
)