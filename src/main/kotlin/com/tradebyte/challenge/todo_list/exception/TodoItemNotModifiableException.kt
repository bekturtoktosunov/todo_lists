package com.tradebyte.challenge.todo_list.exception

import java.util.UUID

class TodoItemNotModifiableException(id: UUID, reason: String) :
    RuntimeException("Todo item with id $id can't be modified (reason: $reason)")