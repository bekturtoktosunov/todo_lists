package com.tradebyte.challenge.todo_list.model.dto

import com.tradebyte.challenge.todo_list.model.domain.TodoItem
import com.tradebyte.challenge.todo_list.model.domain.TodoItemStatus

fun CreateTodoRequest.toDomain(): TodoItem = TodoItem(description = description, dueDateTime = dueDateTime)

fun TodoItem.toResponse(): TodoResponse =
    TodoResponse(
        id = id,
        description = description,
        status = status.toResponse(),
        creationDateTime = creationDateTime,
        dueDateTime = dueDateTime,
        doneDateTime = doneDateTime
    )

fun TodoItemStatus.toResponse(): TodoStatusResponse =
    TodoStatusResponse.entries
        .find { it.name == name }
        ?: throw IllegalArgumentException("Unknown status: $name")