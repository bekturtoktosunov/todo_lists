package com.tradebyte.challenge.todolist.exception

import java.util.UUID

class TodoItemNotModifiableException(
    id: UUID,
    reason: String,
) : RuntimeException("Todo item with id $id can't be modified (reason: $reason)")
