package com.tradebyte.challenge.todolist.model.entity

import com.tradebyte.challenge.todolist.model.domain.TodoItemStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Version
import java.time.Instant
import java.util.UUID

@Entity
class TodoItemEntity(
    @Id
    var id: UUID,
    @Column(length = 1000)
    var description: String,
    @Enumerated(EnumType.STRING)
    var status: TodoItemStatus,
    var creationDateTime: Instant,
    var dueDateTime: Instant,
    var doneDateTime: Instant? = null,
    @Version
    var version: Long = 0,
)
