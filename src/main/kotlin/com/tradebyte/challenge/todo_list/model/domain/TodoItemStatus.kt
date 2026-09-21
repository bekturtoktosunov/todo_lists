package com.tradebyte.challenge.todo_list.model.domain

enum class TodoItemStatus {
    NOT_DONE {
        override fun canTransitionTo(target: TodoItemStatus): Boolean =
            when (target) {
                DONE, PAST_DUE -> true
                else -> false
            }
    },
    DONE {
        override fun canTransitionTo(target: TodoItemStatus): Boolean =
            when (target) {
                NOT_DONE -> true
                else -> false
            }
    },
    PAST_DUE {
        override fun canTransitionTo(target: TodoItemStatus): Boolean = false
    };

    open fun canTransitionTo(target: TodoItemStatus): Boolean = false

    open fun allowsModification(): Boolean = this != PAST_DUE
}