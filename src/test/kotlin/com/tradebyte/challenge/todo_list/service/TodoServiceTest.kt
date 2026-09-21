package com.tradebyte.challenge.todo_list.service

import com.tradebyte.challenge.todo_list.exception.TodoItemNotFoundException
import com.tradebyte.challenge.todo_list.exception.TodoItemNotModifiableException
import com.tradebyte.challenge.todo_list.model.domain.TodoItem
import com.tradebyte.challenge.todo_list.model.domain.TodoItemStatus
import com.tradebyte.challenge.todo_list.model.entity.toEntity
import com.tradebyte.challenge.todo_list.repository.TodoJpaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.ZoneOffset
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID

class TodoServiceTest {

    private val repository = mock<TodoJpaRepository>()
    private val clock = fixedClock()
    private val service = TodoService(repository, clock)

    @Test
    fun `should save item and return result`() {
        // Given
        val item = buildValidNotDoneItem()
        val itemEntity = item.toEntity()
        whenever(repository.save(any())).thenReturn(itemEntity)

        // When
        val created = service.create(item)

        // Then
        verify(repository).save(any())
        assertEquals(item.id, created.id)
    }

    @Test
    fun `should throw IlligalStateException when due date is not in the future`() {
        // Given
        val item = buildNotDoneItemWithPastDueDateTime()

        // When Then
        assertThrows(IllegalStateException::class.java) {
            service.create(item)
        }
    }

    @Test
    fun `should find item by id and return result`() {
        // Given
        val id = UUID.randomUUID()
        val itemEntity = buildValidNotDoneItem().toEntity()
        whenever(repository.findById(id)).thenReturn(Optional.of(itemEntity))

        // When
        val found = service.find(id)

        // Then
        verify(repository).findById(id)
        assertEquals(itemEntity.id, found.id)
    }

    @Test
    fun `should throw TodoItemNotFoundException when item not found`() {
        // Given
        val id = UUID.randomUUID()
        whenever(repository.findById(id)).thenReturn(Optional.empty())

        // When & Then
        assertThrows(TodoItemNotFoundException::class.java) {
            service.find(id)
        }
    }

    @Test
    fun `should update item description and return result`() {
        // Given
        val itemEntity = buildValidNotDoneItem().toEntity()
        val id = itemEntity.id
        whenever(repository.findById(id)).thenReturn(Optional.of(itemEntity))

        // When
        val result = service.updateDescription(id, "new description")

        // Then
        verify(repository).findById(id)
        assertEquals(itemEntity.id, result.id)
        assertEquals(itemEntity.description, result.description)
    }

    @Test
    fun `should throw TodoItemNotFoundException when item not found by description update`() {
        // Given
        val id = UUID.randomUUID()
        whenever(repository.findById(id)).thenReturn(Optional.empty())

        // When & Then
        assertThrows(TodoItemNotFoundException::class.java) {
            service.updateDescription(id, "new description")
        }
    }

    @Test
    fun `should throw TodoItemNotModifiableException when updating item with state PAST_DUE`() {
        // Given
        val id = UUID.randomUUID()
        val itemWithDueDate = buildPastDueItem()
        val itemEntity = itemWithDueDate.toEntity()
        whenever(repository.findById(id)).thenReturn(Optional.of(itemEntity))

        // When & Then
        assertThrows(TodoItemNotModifiableException::class.java) {
            service.updateDescription(id, "new description")
        }
    }

    private fun buildValidNotDoneItem(): TodoItem {
        val now = Instant.now(clock)
        return TodoItem(
            id = UUID.randomUUID(),
            description = "Some description",
            status = TodoItemStatus.NOT_DONE,
            creationDateTime = now.minus(1, ChronoUnit.DAYS),
            dueDateTime = now.plus(1, ChronoUnit.DAYS),
        )
    }

    private fun buildNotDoneItemWithPastDueDateTime(): TodoItem {
        val now = Instant.now(clock)
        return TodoItem(
            id = UUID.randomUUID(),
            description = "Some description",
            status = TodoItemStatus.NOT_DONE,
            creationDateTime = now.minus(2, ChronoUnit.DAYS),
            dueDateTime = now.minus(1, ChronoUnit.DAYS),
        )
    }

    private fun buildPastDueItem(): TodoItem {
        val now = Instant.now(clock)
        return TodoItem(
            id = UUID.randomUUID(),
            description = "Some description",
            status = TodoItemStatus.PAST_DUE,
            creationDateTime = now.minus(2, ChronoUnit.DAYS),
            dueDateTime = now.plus(1, ChronoUnit.DAYS),
        )
    }

    private fun fixedClock(): Clock {
        val fixedInstant = Instant.parse("2026-09-20T00:00:00Z")
        return Clock.fixed(fixedInstant, ZoneOffset.UTC)
    }
}