package com.tradebyte.challenge.todo_list.exception

import com.tradebyte.challenge.todo_list.model.domain.TodoItemStatus
import java.util.UUID

class InvalidTodoStatusTransitionException(
    id: UUID,
    sourceStatus: TodoItemStatus,
    targetStatus: TodoItemStatus
) : RuntimeException("Cannot transition item with id $id from $sourceStatus to $targetStatus")