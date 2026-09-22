package com.tradebyte.challenge.todo_list.repository

import com.tradebyte.challenge.todo_list.model.domain.TodoItemStatus
import com.tradebyte.challenge.todo_list.model.entity.TodoItemEntity
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TodoJpaRepository : JpaRepository<TodoItemEntity, UUID> {
    fun findAllByStatus(status: TodoItemStatus, sort: Sort): List<TodoItemEntity>
}