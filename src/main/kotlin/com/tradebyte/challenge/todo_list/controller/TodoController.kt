package com.tradebyte.challenge.todo_list.controller

import com.tradebyte.challenge.todo_list.model.dto.CreateTodoRequest
import com.tradebyte.challenge.todo_list.model.dto.TodoResponse
import com.tradebyte.challenge.todo_list.model.dto.toDomain
import com.tradebyte.challenge.todo_list.model.dto.toResponse
import com.tradebyte.challenge.todo_list.service.TodoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.time.Clock

@Tag(name = "Todo list", description = "Operations with todo list items")
@RestController
@RequestMapping("/todo-list/v1/items")
class TodoController(
    private val service: TodoService,
    private val clock: Clock
) {
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
            ApiResponse(
                responseCode = "400",
                description = "Validation error",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = ProblemDetail::class)
                    )
                ]
            )]
    )
    @PostMapping
    fun add(@Valid @RequestBody request: CreateTodoRequest): ResponseEntity<TodoResponse> {
        val item = request.toDomain(clock)
        val created = service.create(item)
        return ResponseEntity
            .created(URI.create("/todo-list/v1/items/${created.id}"))
            .body(created.toResponse())
    }
}