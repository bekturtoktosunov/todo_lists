package com.tradebyte.challenge.todolist.exception

import com.tradebyte.challenge.todolist.model.domain.TodoItemStatus
import java.util.UUID

class InvalidTodoStatusTransitionException(
    id: UUID,
    sourceStatus: TodoItemStatus,
    targetStatus: TodoItemStatus,
) : RuntimeException("Cannot transition item with id $id from $sourceStatus to $targetStatus")
