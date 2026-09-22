package com.tradebyte.challenge.todo_list.service

import com.tradebyte.challenge.todo_list.exception.InvalidTodoStatusTransitionException
import com.tradebyte.challenge.todo_list.exception.TodoItemNotFoundException
import com.tradebyte.challenge.todo_list.exception.TodoItemNotModifiableException
import com.tradebyte.challenge.todo_list.model.domain.TodoItem
import com.tradebyte.challenge.todo_list.model.domain.TodoItemStatus
import com.tradebyte.challenge.todo_list.model.entity.toEntity
import com.tradebyte.challenge.todo_list.repository.TodoJpaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Sort
import java.time.ZoneOffset
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.UUID
import kotlin.jvm.java

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
        val itemEntity = buildValidNotDoneItem().toEntity()
        val id = itemEntity.id
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
        val newDescription = "new description"

        // When
        val result = service.updateDescription(id, newDescription)

        // Then
        verify(repository).findById(id)
        assertEquals(itemEntity.id, result.id)
        assertEquals(newDescription, itemEntity.description)
        assertEquals(newDescription, result.description)
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
        val itemWithDueDate = buildPastDueItem()
        val itemEntity = itemWithDueDate.toEntity()
        val id = itemEntity.id
        whenever(repository.findById(id)).thenReturn(Optional.of(itemEntity))

        // When & Then
        assertThrows(TodoItemNotModifiableException::class.java) {
            service.updateDescription(id, "new description")
        }
    }

    @Test
    fun `should throw TodoItemNotModifiableException when updating item's description with pastDateTime in the past`() {
        // Given
        val itemEntity = buildNotDoneItemWithPastDueDateTime().toEntity()
        val id = itemEntity.id
        whenever(repository.findById(id)).thenReturn(Optional.of(itemEntity))

        // When & Then
        assertThrows(TodoItemNotModifiableException::class.java) {
            service.updateDescription(id, "new description")
        }
    }

    @Test
    fun `should mark NOT_DONE item to DONE and return result`() {
        // Given
        val itemEntity = buildValidNotDoneItem().toEntity()
        val id = itemEntity.id
        whenever(repository.findById(id)).thenReturn(Optional.of(itemEntity))

        // When
        val result = service.updateStatus(id, TodoItemStatus.DONE)

        // Then
        assertEquals(id, result.id)
        assertEquals(TodoItemStatus.DONE, result.status)
        assertEquals(clock.instant(), result.doneDateTime)
    }

    @Test
    fun `should mark DONE item to NOT_DONE and return result`() {
        // Given
        val itemEntity = buildValidDoneItem().toEntity()
        val id = itemEntity.id
        whenever(repository.findById(id)).thenReturn(Optional.of(itemEntity))

        // When
        val result = service.updateStatus(id, TodoItemStatus.NOT_DONE)

        // Then
        assertEquals(id, result.id)
        assertEquals(TodoItemStatus.NOT_DONE, result.status)
        assertNull(result.doneDateTime)
    }

    @Test
    fun `should make no change when source status is target status`() {
        // Given
        val itemEntity = buildValidDoneItem().toEntity()
        val id = itemEntity.id
        val originalDoneDateTime = itemEntity.doneDateTime
        whenever(repository.findById(id)).thenReturn(Optional.of(itemEntity))

        // When
        val result = service.updateStatus(id, TodoItemStatus.DONE)

        // Then
        assertEquals(id, result.id)
        assertEquals(TodoItemStatus.DONE, result.status)
        assertEquals(originalDoneDateTime, itemEntity.doneDateTime)
        assertEquals(originalDoneDateTime, result.doneDateTime)
    }

    @Test
    fun `should throw TodoItemNotFoundException when item not found by status update`() {
        // Given
        val id = UUID.randomUUID()
        whenever(repository.findById(id)).thenReturn(Optional.empty())

        // When & Then
        assertThrows(TodoItemNotFoundException::class.java) {
            service.updateStatus(id, TodoItemStatus.DONE)
        }
    }

    @Test
    fun `should throw TodoItemNotModifiableException when updating item's state with state PAST_DUE`() {
        // Given
        val itemEntity = buildPastDueItem().toEntity()
        val id = itemEntity.id
        whenever(repository.findById(id)).thenReturn(Optional.of(itemEntity))

        // When & Then
        assertThrows(TodoItemNotModifiableException::class.java) {
            service.updateStatus(id, TodoItemStatus.DONE)
        }
    }

    @Test
    fun `should throw TodoItemNotModifiableException when updating item's state with pastDateTime in the past`() {
        // Given
        val itemEntity = buildNotDoneItemWithPastDueDateTime().toEntity()
        val id = itemEntity.id
        whenever(repository.findById(id)).thenReturn(Optional.of(itemEntity))

        // When & Then
        assertThrows(TodoItemNotModifiableException::class.java) {
            service.updateStatus(id, TodoItemStatus.DONE)
        }
    }

    @Test
    fun `should throw InvalidTodoStatusTransitionException when target update status is PAST_DUE`() {
        // Given
        val itemEntity = buildValidNotDoneItem().toEntity()
        val id = itemEntity.id
        whenever(repository.findById(id)).thenReturn(Optional.of(itemEntity))

        // When & Then
        assertThrows(InvalidTodoStatusTransitionException::class.java) {
            service.updateStatus(id, TodoItemStatus.PAST_DUE)
        }
    }

    @Test
    fun `should return all todo items when status filter is null`() {
        // Given
        val items = buildValidItems()
        val entities = items.map { it.toEntity() }
        whenever(repository.findAll(DEFAULT_SORT)).thenReturn(entities)

        // When
        val result = service.findAll(null)

        // Then
        assertEquals(items, result)
        verify(repository).findAll(DEFAULT_SORT)
    }

    @ParameterizedTest
    @EnumSource(TodoItemStatus::class)
    fun `should return todo items matching given status`(status: TodoItemStatus) {
        // Given
        val item = when (status) {
            TodoItemStatus.DONE -> buildValidDoneItem()
            TodoItemStatus.NOT_DONE -> buildValidNotDoneItem()
            TodoItemStatus.PAST_DUE -> buildPastDueItem()
        }
        val items = listOf(item)
        val entities = items.map { it.toEntity() }
        whenever(repository.findAllByStatus(status, DEFAULT_SORT)).thenReturn(entities)

        // When
        val result = service.findAll(status)

        // Then
        assertEquals(items, result)
        verify(repository).findAllByStatus(status, DEFAULT_SORT)
    }

    @Test
    fun `should return empty list when no items exist`() {
        // Given
        whenever(repository.findAll(DEFAULT_SORT)).thenReturn(emptyList())

        // When
        val result = service.findAll(null)

        // Then
        assertTrue(result.isEmpty())
        verify(repository).findAll(DEFAULT_SORT)
    }

    @Test
    fun `should return empty list when no items found for matching status`() {
        // Given
        val status = TodoItemStatus.DONE
        whenever(repository.findAllByStatus(status, DEFAULT_SORT)).thenReturn(emptyList())

        // When
        val result = service.findAll(status)

        // Then
        assertTrue(result.isEmpty())
        verify(repository).findAllByStatus(status, DEFAULT_SORT)
    }

    private fun buildValidNotDoneItem(): TodoItem {
        val now = clock.instant()
        return TodoItem(
            id = UUID.randomUUID(),
            description = "Some description",
            status = TodoItemStatus.NOT_DONE,
            creationDateTime = now.minus(1, ChronoUnit.DAYS),
            dueDateTime = now.plus(1, ChronoUnit.DAYS),
        )
    }

    private fun buildNotDoneItemWithPastDueDateTime(): TodoItem {
        val now = clock.instant()
        return TodoItem(
            id = UUID.randomUUID(),
            description = "Some description",
            status = TodoItemStatus.NOT_DONE,
            creationDateTime = now.minus(2, ChronoUnit.DAYS),
            dueDateTime = now.minus(1, ChronoUnit.DAYS),
        )
    }

    private fun buildValidDoneItem(): TodoItem {
        val now = clock.instant()
        return TodoItem(
            id = UUID.randomUUID(),
            description = "Some description",
            status = TodoItemStatus.DONE,
            creationDateTime = now.minus(2, ChronoUnit.DAYS),
            doneDateTime = now.minus(1, ChronoUnit.DAYS),
            dueDateTime = now.plus(1, ChronoUnit.DAYS),
        )
    }

    private fun buildPastDueItem(): TodoItem {
        val now = clock.instant()
        return TodoItem(
            id = UUID.randomUUID(),
            description = "Some description",
            status = TodoItemStatus.PAST_DUE,
            creationDateTime = now.minus(2, ChronoUnit.DAYS),
            dueDateTime = now.minus(1, ChronoUnit.DAYS),
        )
    }

    private fun buildValidItems(): List<TodoItem> =
        listOf(
            buildValidDoneItem(),
            buildValidNotDoneItem(),
            buildPastDueItem()
        )

    private fun fixedClock(): Clock {
        val fixedInstant = Instant.parse("2026-09-20T00:00:00Z")
        return Clock.fixed(fixedInstant, ZoneOffset.UTC)
    }

    companion object {
        val DEFAULT_SORT =
            Sort.by(
                Sort.Order.asc("creationDateTime"),
                Sort.Order.asc("id")
            )
    }
}