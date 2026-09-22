package com.tradebyte.challenge.todo_list.exception

import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.Clock

@ControllerAdvice
class GlobalExceptionHandler(private val clock: Clock) {

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(exception: IllegalStateException): ProblemDetail =
        problemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Bad Request",
            detail = exception.message ?: "Invalid state",
            errorCode = ApiErrorCode.VALIDATION_FAILED
        )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationError(exception: MethodArgumentNotValidException): ProblemDetail {
        val problemDetail = problemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Bad Request",
            detail = "Request validation failed",
            errorCode = ApiErrorCode.VALIDATION_FAILED
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
            errorCode = ApiErrorCode.TODO_ITEM_NOT_FOUND
        )

    @ExceptionHandler(OptimisticLockingFailureException::class)
    fun handleOptimisticLock(): ProblemDetail =
        problemDetail(
            status = HttpStatus.CONFLICT,
            title = "Conflict",
            detail = "Todo list item was modified concurrently. Reload it and try again",
            errorCode = ApiErrorCode.CONCURRENT_MODIFICATION
        )

    @ExceptionHandler(TodoItemNotModifiableException::class)
    fun handleTodoItemNotModifiable(exception: TodoItemNotModifiableException): ProblemDetail =
        problemDetail(
            status = HttpStatus.CONFLICT,
            title = "Conflict",
            detail = exception.message ?: "Item can't be modified",
            errorCode = ApiErrorCode.TODO_ITEM_IMMUTABLE
        )

    @ExceptionHandler(InvalidTodoStatusTransitionException::class)
    fun handleInvalidTodoStatusTransition(exception: InvalidTodoStatusTransitionException): ProblemDetail =
        problemDetail(
            status = HttpStatus.CONFLICT,
            title = "Conflict",
            detail = exception.message ?: "Invalid todo item status transition",
            errorCode = ApiErrorCode.INVALID_STATUS_TRANSITION
        )

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(): ProblemDetail =
        problemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Bad Request",
            detail = "Request body is missing or invalid",
            errorCode = ApiErrorCode.INVALID_REQUEST_BODY
        )

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleInvalidRequestParameter(exception: MethodArgumentTypeMismatchException): ProblemDetail =
        problemDetail(
            status = HttpStatus.BAD_REQUEST,
            title = "Bad Request",
            detail = "Request parameter is invalid (${exception.name}=${exception.value})",
            errorCode = ApiErrorCode.INVALID_REQUEST_PARAM
        )

    private fun problemDetail(
        status: HttpStatus,
        title: String,
        detail: String,
        errorCode: ApiErrorCode
    ): ProblemDetail {
        val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
        problemDetail.title = title
        problemDetail.setProperty("error_code", errorCode)
        problemDetail.setProperty("timestamp", clock.instant())
        return problemDetail
    }
}