package com.tradebyte.challenge.todo_list.model.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreateTodoRequest(
    @field:Schema(description = "Description of the task", example = "Finish coding challenge")
    @field:NotBlank
    @field:Size(max = 1000)
    val description: String,

    @get:JsonProperty("due_datetime")
    @field:Schema(name = "due_datetime", description = "Due date in ISO format", example = "2026-09-22T00:00:00Z")
    val dueDateTime: Instant
)