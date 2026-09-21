package com.tradebyte.challenge.todo_list.service

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
import java.util.UUID

class TodoServiceTest {

    private val repository = mock<TodoJpaRepository>()
    private val clock = fixedClock()
    private val service = TodoService(repository, clock)

    @Test
    fun `should save item and return result`() {
        // Given
        val item = createValidItem()
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
        val item = createItemWithPastDueDateTime()

        // When Then
        assertThrows(IllegalStateException::class.java) {
            service.create(item)
        }
    }

    private fun createValidItem(): TodoItem {
        val now = Instant.now(clock)
        return TodoItem(
            id = UUID.randomUUID(),
            description = "Some description",
            status = TodoItemStatus.NOT_DONE,
            creationDateTime = now.minus(1, ChronoUnit.DAYS),
            dueDateTime = now.plus(1, ChronoUnit.DAYS),
        )
    }

    private fun createItemWithPastDueDateTime(): TodoItem {
        val now = Instant.now(clock)
        return TodoItem(
            id = UUID.randomUUID(),
            description = "Some description",
            status = TodoItemStatus.NOT_DONE,
            creationDateTime = now.minus(2, ChronoUnit.DAYS),
            dueDateTime = now.minus(1, ChronoUnit.DAYS),
        )
    }

    private fun fixedClock(): Clock {
        val fixedInstant = Instant.parse("2026-09-20T00:00:00Z")
        return Clock.fixed(fixedInstant, ZoneOffset.UTC)
    }
}