package com.tradebyte.challenge.todo_list.service

import com.tradebyte.challenge.todo_list.model.domain.TodoItem
import com.tradebyte.challenge.todo_list.model.entity.toDomain
import com.tradebyte.challenge.todo_list.model.entity.toEntity
import com.tradebyte.challenge.todo_list.repository.TodoJpaRepository
import org.springframework.stereotype.Service
import java.time.Clock

@Service
class TodoService(
    private val repository: TodoJpaRepository,
    private val clock: Clock
) {
    /**
     * Creates new todo list item
     * Preconditions: item.dueDateTime must be in the future (after clock.instant())
     * Throws: IllegalStateException when dueDateTime is in the past
     */
    fun create(item: TodoItem): TodoItem {
        check(item.dueDateTime > clock.instant()) {
            "Due date must be in the future"
        }

        val itemEntity = item.toEntity()
        val saved = repository.save(itemEntity)

        return saved.toDomain()
    }
}