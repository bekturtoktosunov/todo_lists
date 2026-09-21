package com.tradebyte.challenge.todo_list.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.Clock

@ControllerAdvice
class GlobalExceptionHandler(private val clock: Clock) {

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(exception: IllegalStateException): ProblemDetail =
        problemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Bad Request",
            detail = exception.message ?: "Invalid state",
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(exception: MethodArgumentNotValidException): ProblemDetail {
        val problemDetail = problemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Bad Request",
            detail = "Request validation failed",
        )
        val errors = exception.bindingResult.fieldErrors.associate { fieldError ->
            fieldError.field to (fieldError.defaultMessage ?: "Invalid field value")
        }
        problemDetail.setProperty("errors", errors)
        return problemDetail
    }

    @ExceptionHandler(TodoItemNotFoundException::class)
    fun handleTodoItemNotFound(exception: TodoItemNotFoundException): ProblemDetail =
        problemDetail(
            status = HttpStatus.NOT_FOUND,
            title = "Not Found",
            detail = "Todo list item with id ${exception.itemId} not found",
        )

    private fun problemDetail(
        status: HttpStatus,
        title: String,
        detail: String
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
        problemDetail.title = title
        problemDetail.setProperty("timestamp", clock.instant())
        return problemDetail
    }
}