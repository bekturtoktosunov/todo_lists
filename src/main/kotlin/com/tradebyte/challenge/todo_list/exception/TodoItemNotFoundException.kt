package com.tradebyte.challenge.todo_list.exception

import java.util.UUID

class TodoItemNotFoundException(val itemId: UUID) : RuntimeException("Todo list item with id $itemId not found")