package com.tradebyte.challenge.todolist.service

import com.tradebyte.challenge.todolist.exception.InvalidTodoStatusTransitionException
import com.tradebyte.challenge.todolist.exception.TodoItemNotFoundException
import com.tradebyte.challenge.todolist.exception.TodoItemNotModifiableException
import com.tradebyte.challenge.todolist.model.domain.TodoItem
import com.tradebyte.challenge.todolist.model.domain.TodoItemStatus
import com.tradebyte.challenge.todolist.model.entity.TodoItemEntity
import com.tradebyte.challenge.todolist.model.entity.toDomain
import com.tradebyte.challenge.todolist.model.entity.toEntity
import com.tradebyte.challenge.todolist.repository.TodoJpaRepository
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.util.UUID

@Service
class TodoService(
    private val repository: TodoJpaRepository,
    private val clock: Clock,
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
    fun find(id: UUID): TodoItem = repository
        .findByIdOrNull(id)
        ?.toDomain()
        ?: throw TodoItemNotFoundException(id)

    /**
     * Updates description of todo list item
     * Throws: TodoItemNotFoundException when no item with given id found
     * Throws: TodoItemNotModifiableException when status forbids modifications (PAST_DUE)
     */
    @Transactional
    fun updateDescription(
        id: UUID,
        newDescription: String,
    ): TodoItem {
        val itemEntity = repository.findByIdOrNull(id) ?: throw TodoItemNotFoundException(id)

        validateItemIsModifiable(itemEntity, id)

        itemEntity.description = newDescription

        return itemEntity.toDomain()
    }

    /**
     * Updates status of todo list item
     * Returns item with no change if it's already in target status
     * Throws: TodoItemNotFoundException when no item with given id found
     * Throws: TodoItemNotModifiableException when status forbids modifications (PAST_DUE)
     * Throws: InvalidTodoStatusTransitionException when source status cannot transition to target status
     */
    @Transactional
    fun updateStatus(
        id: UUID,
        targetStatus: TodoItemStatus,
    ): TodoItem {
        val itemEntity = repository.findByIdOrNull(id) ?: throw TodoItemNotFoundException(id)

        validateItemIsModifiable(itemEntity, id)

        if (itemEntity.status == targetStatus) return itemEntity.toDomain()

        if (!itemEntity.status.canTransitionByUserTo(targetStatus)) {
            throw InvalidTodoStatusTransitionException(id, itemEntity.status, targetStatus)
        }

        itemEntity.status = targetStatus

        itemEntity.doneDateTime = if (itemEntity.status == TodoItemStatus.DONE) clock.instant() else null

        return itemEntity.toDomain()
    }

    private fun validateItemIsModifiable(
        itemEntity: TodoItemEntity,
        id: UUID,
    ) {
        val isPastDue = itemEntity.status == TodoItemStatus.NOT_DONE && itemEntity.dueDateTime < clock.instant()

        if (!itemEntity.status.allowsModification()) {
            throw TodoItemNotModifiableException(id, "status ${itemEntity.status} doesn't allow changes")
        } else if (isPastDue) {
            throw TodoItemNotModifiableException(id, "Item is past due and cannot be modified")
        }
    }

    /**
     * Finds existing todo items by status
     * Returns all items if status is null
     * Returns empty List if no items found
     */
    fun findAll(status: TodoItemStatus?): List<TodoItem> {
        val sort =
            Sort.by(
                Sort.Order.asc("creationDateTime"),
                Sort.Order.asc("id"),
            )

        val items =
            if (status == null) {
                repository.findAll(sort)
            } else {
                repository.findAllByStatus(status, sort)
            }

        return items.map { item -> item.toDomain() }
    }
}
