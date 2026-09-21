package com.tradebyte.challenge.todo_list.model.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class CreateTodoRequest(
    @field:NotBlank
    @field:Size(max = 1000)
    val description: String,
    val dueDateTime: Instant
)