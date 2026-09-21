package com.tradebyte.challenge.todo_list.model.entity

import com.tradebyte.challenge.todo_list.model.domain.TodoItem

fun TodoItem.toEntity(): TodoItemEntity =
    TodoItemEntity(
        id = id,
        description = description,
        status = status,
        creationDateTime = creationDateTime,
        dueDateTime = dueDateTime,
        doneDateTime = doneDateTime
    )

fun TodoItemEntity.toDomain(): TodoItem =
    TodoItem(
        id = id,
        description = description,
        status = status,
        creationDateTime = creationDateTime,
        dueDateTime = dueDateTime,
        doneDateTime = doneDateTime
    )