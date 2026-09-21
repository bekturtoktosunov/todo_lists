package com.tradebyte.challenge.todo_list.model.entity

import com.tradebyte.challenge.todo_list.model.domain.TodoItemStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Version
import java.util.UUID
import java.time.Instant

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
    var version: Long = 0
)