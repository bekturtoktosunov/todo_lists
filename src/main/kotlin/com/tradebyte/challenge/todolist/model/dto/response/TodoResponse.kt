package com.tradebyte.challenge.todolist.model.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID

data class TodoResponse(
    val id: UUID,
    val description: String,
    val status: TodoStatusResponse,
    @get:JsonProperty("creation_datetime")
    @field:Schema(name = "creation_datetime")
    val creationDateTime: Instant,
    @get:JsonProperty("due_datetime")
    @field:Schema(name = "due_datetime")
    val dueDateTime: Instant,
    @get:JsonProperty("done_datetime")
    @field:Schema(name = "done_datetime")
    val doneDateTime: Instant?,
)
