package com.tradebyte.challenge.todo_list.controller

import com.tradebyte.challenge.todo_list.model.dto.request.CreateTodoRequest
import com.tradebyte.challenge.todo_list.model.dto.response.TodoResponse
import com.tradebyte.challenge.todo_list.model.dto.request.UpdateTodoDescriptionRequest
import com.tradebyte.challenge.todo_list.model.dto.request.UpdateTodoStatusRequest
import com.tradebyte.challenge.todo_list.model.dto.toDomain
import com.tradebyte.challenge.todo_list.model.dto.toResponse
import com.tradebyte.challenge.todo_list.service.TodoService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI
import java.time.Clock
import java.util.*

@RestController
@RequestMapping("/todo-list/v1/items")
class TodoController(
    private val service: TodoService,
    private val clock: Clock
) : TodoApi {
    @PostMapping
    override fun add(@Valid @RequestBody request: CreateTodoRequest): ResponseEntity<TodoResponse> {
        val item = request.toDomain(clock)
        val created = service.create(item)
        return ResponseEntity
            .created(URI.create("/todo-list/v1/items/${created.id}"))
            .body(created.toResponse())
    }

    @GetMapping("/{id}")
    override fun getItem(@PathVariable id: UUID): TodoResponse =
        service.find(id).toResponse()

    @PatchMapping("/{id}")
    override fun updateItemDescription(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateTodoDescriptionRequest
    ): TodoResponse =
        service.updateDescription(id, request.description).toResponse()

    @PutMapping("/{id}/status")
    override fun updateItemStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateTodoStatusRequest
    ): TodoResponse {
        val targetStatus = request.status.toDomain()
        val updatedItem = service.updateStatus(id, targetStatus)
        return updatedItem.toResponse()
    }
}