package com.tradebyte.challenge.todo_list.controller

import com.tradebyte.challenge.todo_list.model.dto.CreateTodoRequest
import com.tradebyte.challenge.todo_list.model.dto.TodoResponse
import com.tradebyte.challenge.todo_list.model.dto.toDomain
import com.tradebyte.challenge.todo_list.model.dto.toResponse
import com.tradebyte.challenge.todo_list.service.TodoService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/todo-list/v1/items")
class TodoController(
    private val service: TodoService
) {
    @PostMapping
    fun add(@Valid @RequestBody request: CreateTodoRequest): ResponseEntity<TodoResponse> {
        val item = request.toDomain()
        val created = service.create(item)
        return ResponseEntity
            .created(URI.create("/todo-list/v1/items/${created.id}"))
            .body(created.toResponse())
    }
}