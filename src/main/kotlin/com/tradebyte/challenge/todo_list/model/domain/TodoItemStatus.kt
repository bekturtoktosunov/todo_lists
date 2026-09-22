package com.tradebyte.challenge.todo_list.model.domain

enum class TodoItemStatus {
    NOT_DONE {
        override fun canTransitionByUserTo(target: TodoItemStatus): Boolean =
            when (target) {
                DONE -> true
                else -> false
            }
    },
    DONE {
        override fun canTransitionByUserTo(target: TodoItemStatus): Boolean =
            when (target) {
                NOT_DONE -> true
                else -> false
            }
    },
    PAST_DUE {
        override fun canTransitionByUserTo(target: TodoItemStatus): Boolean = false
    };

    open fun canTransitionByUserTo(target: TodoItemStatus): Boolean = false

    open fun allowsModification(): Boolean = this != PAST_DUE
}