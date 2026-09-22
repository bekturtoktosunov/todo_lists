package com.tradebyte.challenge.todo_list.repository

import com.tradebyte.challenge.todo_list.model.domain.TodoItemStatus
import com.tradebyte.challenge.todo_list.model.entity.TodoItemEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface TodoJpaRepository : JpaRepository<TodoItemEntity, UUID> {
    fun findAllByStatus(status: TodoItemStatus, sort: Sort): List<TodoItemEntity>

    @Modifying
    @Query(
        """
            update TodoItemEntity item
            set item.status = PAST_DUE,
                item.version = item.version + 1
            where item.status = NOT_DONE
                and item.dueDateTime < :now
        """
    )
    fun markItemsPastDue(@Param("now") now: Instant): Int
}