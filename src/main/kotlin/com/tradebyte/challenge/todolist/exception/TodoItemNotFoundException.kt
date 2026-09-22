package com.tradebyte.challenge.todolist.exception

import java.util.UUID

class TodoItemNotFoundException(
    val itemId: UUID,
) : RuntimeException("Todo list item with id $itemId not found")
