package com.tradebyte.challenge.todo_list.model.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateTodoDescriptionRequest(
    @field:Schema(description = "Description of the task", example = "Finish coding challenge")
    @field:NotBlank
    @field:Size(max = 1000)
    val description: String
)