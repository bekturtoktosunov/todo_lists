package com.tradebyte.challenge.todo_list.service

import com.tradebyte.challenge.todo_list.exception.TodoItemNotFoundException
import com.tradebyte.challenge.todo_list.model.domain.TodoItem
import com.tradebyte.challenge.todo_list.model.entity.toDomain
import com.tradebyte.challenge.todo_list.model.entity.toEntity
import com.tradebyte.challenge.todo_list.repository.TodoJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

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

    /**
     * Finds existing todo list item
     * Throws: TodoItemNotFoundException when no item with given id found
     */
    fun find(id: UUID): TodoItem =
        repository.findByIdOrNull(id)
            ?.toDomain()
            ?: throw TodoItemNotFoundException(id)

    /**
     * Updates description of todo list item
     * Throws: TodoItemNotFoundException when no item with given id found
     */
    @Transactional
    fun updateDescription(id: UUID, newDescription: String): TodoItem {
        val itemEntity = repository.findByIdOrNull(id) ?: throw TodoItemNotFoundException(id)

        itemEntity.description = newDescription

        return itemEntity.toDomain()
    }
}