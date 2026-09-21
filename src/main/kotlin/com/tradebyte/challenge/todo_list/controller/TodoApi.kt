package com.tradebyte.challenge.todo_list.controller

import com.tradebyte.challenge.todo_list.model.dto.CreateTodoRequest
import com.tradebyte.challenge.todo_list.model.dto.TodoResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import java.util.UUID

@Tag(name = "Todo list", description = "Operations with todo list items")
interface TodoApi {
    @Operation(summary = "Create a new todo list item")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Item created",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TodoResponse::class)
                    )
                ]
            ),
            ApiResponse(ref = "#/components/responses/BadRequest")]
    )
    fun add(request: CreateTodoRequest): ResponseEntity<TodoResponse>

    @Operation(summary = "Get a todo item by id")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Item returned",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = TodoResponse::class)
                    )
                ]
            ),
            ApiResponse(ref = "#/components/responses/NotFound")
        ]
    )
    fun getItem(id: UUID): TodoResponse
}