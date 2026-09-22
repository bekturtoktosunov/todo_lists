package com.tradebyte.challenge.todolist.repository

import com.tradebyte.challenge.todolist.model.domain.TodoItemStatus
import com.tradebyte.challenge.todolist.model.entity.TodoItemEntity
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.dao.OptimisticLockingFailureException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.Test

@DataJpaTest
class TodoJpaRepositoryTest {
    @Autowired
    lateinit var repository: TodoJpaRepository

    @Autowired
    lateinit var entityManager: EntityManager

    private val now = Instant.parse("2026-09-22T12:00:00Z")

    @Test
    fun `should mark not done item as past due`() {
        // Given
        val item = persistItem(
            status = TodoItemStatus.NOT_DONE,
            dueDateTime = now.minusSeconds(1),
        )
        val originalVersion = item.version
        entityManager.clear()

        // When
        val updatedCount = repository.markItemsPastDue(now)
        entityManager.clear()

        // Then
        val updated = repository.findById(item.id).orElseThrow()

        assertEquals(1, updatedCount)
        assertEquals(TodoItemStatus.PAST_DUE, updated.status)
        assertEquals(originalVersion + 1, updated.version)

        assertEquals(item.description, updated.description)
        assertEquals(item.creationDateTime, updated.creationDateTime)
        assertEquals(item.dueDateTime, updated.dueDateTime)
        assertNull(updated.doneDateTime)
    }

    @Test
    fun `should not update expired item again if executed twice`() {
        // Given
        val item = persistItem(
            status = TodoItemStatus.NOT_DONE,
            dueDateTime = now.minusSeconds(1),
        )
        val originalVersion = item.version
        entityManager.clear()

        // When
        val first = repository.markItemsPastDue(now)
        val second = repository.markItemsPastDue(now.plusSeconds(10))
        entityManager.clear()

        // Then
        val updated = repository.findById(item.id).orElseThrow()

        assertEquals(1, first)
        assertEquals(0, second)
        assertEquals(TodoItemStatus.PAST_DUE, updated.status)
        assertEquals(originalVersion + 1, updated.version)
    }

    @Test
    fun `should leave items unchanged when they didn't expire`() {
        // Given
        val items = buildAndPersistItems().associateBy { it.id }
        entityManager.clear()

        // When
        val updatedCount = repository.markItemsPastDue(now)
        entityManager.clear()

        // Then
        assertEquals(0, updatedCount)

        val itemEntities = repository.findAllById(items.keys)
        assertEquals(items.size, itemEntities.size)

        itemEntities.forEach {
            val item = items[it.id]!!
            assertEquals(item.status, it.status)
            assertEquals(item.version, it.version)
            assertEquals(item.doneDateTime, it.doneDateTime)
        }
    }

    @Test
    fun `should reject saving outdated item`() {
        // Given
        val staleItem = persistItem(
            status = TodoItemStatus.NOT_DONE,
            dueDateTime = now.plusSeconds(3600),
        )
        val originalVersion = staleItem.version
        entityManager.clear()

        val currentItem = repository.findById(staleItem.id).orElseThrow()
        currentItem.description = "First update"
        repository.flush()

        assertEquals(originalVersion + 1, currentItem.version)
        entityManager.clear()

        // When & Then
        staleItem.description = "Outdated update"

        assertThrows(OptimisticLockingFailureException::class.java) {
            repository.saveAndFlush(staleItem)
        }
    }

    @Test
    fun `should reject stale entity update after it expired`() {
        // Given
        val item = persistItem(
            status = TodoItemStatus.NOT_DONE,
            dueDateTime = now.minusSeconds(1),
        )
        val originalVersion = item.version

        val updatedCount = repository.markItemsPastDue(now)

        assertEquals(1, updatedCount)
        assertEquals(originalVersion, item.version)
        assertEquals(TodoItemStatus.NOT_DONE, item.status)

        // When
        item.description = "Update based on stale state"

        // Then
        assertThrows(OptimisticLockingFailureException::class.java) {
            repository.flush()
        }
    }

    private fun persistItem(
        status: TodoItemStatus,
        dueDateTime: Instant,
        doneDateTime: Instant? = null,
    ): TodoItemEntity {
        val item = buildItem(status, dueDateTime, doneDateTime)

        entityManager.persist(item)
        entityManager.flush()

        return item
    }

    private fun buildAndPersistItems(): List<TodoItemEntity> {
        val items = listOf(
            buildItem(
                status = TodoItemStatus.NOT_DONE,
                dueDateTime = now.plusSeconds(1),
            ),
            buildItem(
                status = TodoItemStatus.NOT_DONE,
                dueDateTime = now,
            ),
            buildItem(
                status = TodoItemStatus.DONE,
                dueDateTime = now.minusSeconds(1),
                doneDateTime = now.minusSeconds(60),
            ),
            buildItem(
                status = TodoItemStatus.PAST_DUE,
                dueDateTime = now.minusSeconds(1),
            ),
        )
        items.forEach { entityManager.persist(it) }
        entityManager.flush()

        return items
    }

    private fun buildItem(
        status: TodoItemStatus,
        dueDateTime: Instant,
        doneDateTime: Instant? = null,
    ): TodoItemEntity = TodoItemEntity(
        id = UUID.randomUUID(),
        description = "Test item",
        status = status,
        creationDateTime = now.minus(1, ChronoUnit.DAYS),
        dueDateTime = dueDateTime,
        doneDateTime = doneDateTime,
    )
}
