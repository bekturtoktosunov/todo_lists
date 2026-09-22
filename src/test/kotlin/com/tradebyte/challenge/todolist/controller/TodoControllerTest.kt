package com.tradebyte.challenge.todolist.controller

import com.tradebyte.challenge.todolist.config.TodoTestConfig
import com.tradebyte.challenge.todolist.exception.ApiErrorCode
import com.tradebyte.challenge.todolist.exception.TodoItemNotFoundException
import com.tradebyte.challenge.todolist.exception.TodoItemNotModifiableException
import com.tradebyte.challenge.todolist.model.domain.TodoItem
import com.tradebyte.challenge.todolist.model.domain.TodoItemStatus
import com.tradebyte.challenge.todolist.model.dto.request.EditableTodoStatus
import com.tradebyte.challenge.todolist.model.dto.toDomain
import com.tradebyte.challenge.todolist.service.TodoService
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Clock
import java.time.Instant
import java.util.UUID

@WebMvcTest(controllers = [TodoController::class])
@Import(TodoTestConfig::class)
class TodoControllerTest {
    @Autowired
    lateinit var mvc: MockMvc

    @Autowired
    lateinit var clock: Clock

    @MockitoBean
    lateinit var service: TodoService

    @Test
    fun `should create todo list item`() {
        // Given
        val request = buildValidCreateRequest()
        val mockedResponse = createMockedResponse()
        whenever(service.create(any())).thenReturn(mockedResponse)

        // When
        mvc
            .perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.description").value("testDescription"))
    }

    @Test
    fun `should return bad request when description is blank`() {
        // Given
        val request = buildCreateRequestWithBlankDescription()

        // When
        mvc
            .perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.error_code").value(ApiErrorCode.VALIDATION_FAILED.name))
            .andExpect(jsonPath("$.errors.description").exists())
    }

    @Test
    fun `should return bad request when description is too long`() {
        // Given
        val request = buildCreateRequestWithTooLongDescription()

        // When
        mvc
            .perform(
                post(BASE_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.error_code").value(ApiErrorCode.VALIDATION_FAILED.name))
            .andExpect(jsonPath("$.errors.description").exists())
    }

    @Test
    fun `should find todo list item`() {
        // Given
        val id = UUID.randomUUID()
        val mockedResponse = createMockedResponse(id)
        whenever(service.find(id)).thenReturn(mockedResponse)

        // When
        mvc
            .perform(
                get("$BASE_URL/$id"),
            ) // Then
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id.toString()))
    }

    @Test
    fun `should return not found when item not found`() {
        // Given
        val id = UUID.randomUUID()
        whenever(service.find(id)).thenThrow(TodoItemNotFoundException(id))

        // When
        mvc
            .perform(
                get("$BASE_URL/$id"),
            ) // Then
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.error_code").value(ApiErrorCode.TODO_ITEM_NOT_FOUND.name))
            .andExpect(jsonPath("$.detail").value("Todo list item with id $id not found"))
    }

    @Test
    fun `should update item description and return item`() {
        // Given
        val id = UUID.randomUUID()
        val newDescription = "new description"
        val request = buildValidUpdateDescriptionRequest(newDescription)
        val mockedResponse = createMockedResponse(id, description = newDescription)
        whenever(service.updateDescription(id, newDescription)).thenReturn(mockedResponse)

        // When
        mvc
            .perform(
                patch("$BASE_URL/$id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.description").value(newDescription))
    }

    @Test
    fun `should return bad request when description to update is blank`() {
        // Given
        val id = UUID.randomUUID()
        val request = buildUpdateDescriptionRequestBlank()

        // When
        mvc
            .perform(
                patch("$BASE_URL/$id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.error_code").value(ApiErrorCode.VALIDATION_FAILED.name))
            .andExpect(jsonPath("$.errors.description").exists())
    }

    @Test
    fun `should return bad request when description to update is too long`() {
        // Given
        val id = UUID.randomUUID()
        val request = buildUpdateDescriptionRequestTooLong()

        // When
        mvc
            .perform(
                patch("$BASE_URL/$id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.error_code").value(ApiErrorCode.VALIDATION_FAILED.name))
            .andExpect(jsonPath("$.errors.description").exists())
    }

    @Test
    fun `should return conflict when item was concurrently modified by description update`() {
        // Given
        val id = UUID.randomUUID()
        val description = "Updated description"
        val request = buildValidUpdateDescriptionRequest(description)

        whenever(service.updateDescription(id, description))
            .thenThrow(OptimisticLockingFailureException("Concurrent update"))

        // When
        mvc
            .perform(
                patch("$BASE_URL/$id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.error_code").value(ApiErrorCode.CONCURRENT_MODIFICATION.name))
            .andExpect(jsonPath("$.timestamp").value(clock.instant().toString()))

        verify(service).updateDescription(id, description)
    }

    @Test
    fun `should mark item as DONE and return item`() {
        // Given
        val id = UUID.randomUUID()
        val targetStatus = EditableTodoStatus.DONE
        val request = buildValidUpdateStatusRequest(targetStatus)
        val targetDomainStatus = targetStatus.toDomain()
        val mockedResponse = createMockedResponse(id, status = targetDomainStatus, doneDateTime = clock.instant())
        whenever(service.updateStatus(id, targetDomainStatus)).thenReturn(mockedResponse)

        // When
        mvc
            .perform(
                put("$BASE_URL/$id/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.status").value(targetStatus.value))
            .andExpect(jsonPath("$.done_datetime").value(clock.instant().toString()))
    }

    @Test
    fun `should mark item as NOT_DONE and return item`() {
        // Given
        val id = UUID.randomUUID()
        val targetStatus = EditableTodoStatus.NOT_DONE
        val request = buildValidUpdateStatusRequest(targetStatus)
        val targetDomainStatus = targetStatus.toDomain()
        val mockedResponse = createMockedResponse(id, status = targetDomainStatus)
        whenever(service.updateStatus(id, targetDomainStatus)).thenReturn(mockedResponse)

        // When
        mvc
            .perform(
                put("$BASE_URL/$id/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.status").value(targetStatus.value))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            """{"status":"unknown"}""",
            """{"status":"past due"}""",
            """{"status":null}""",
            """{}""",
            """{"status":brokenJson""",
            "",
        ],
    )
    fun `should return bad request when request or target status are invalid`(request: String) {
        // Given
        val id = UUID.randomUUID()

        // When
        mvc
            .perform(
                put("$BASE_URL/$id/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.error_code").value(ApiErrorCode.INVALID_REQUEST_BODY.name))

        verifyNoInteractions(service)
    }

    @Test
    fun `should return not found when todo item not found`() {
        // Given
        val id = UUID.randomUUID()
        val targetStatus = EditableTodoStatus.NOT_DONE
        val request = buildValidUpdateStatusRequest(targetStatus)
        whenever(service.updateStatus(id, targetStatus.toDomain())).thenThrow(TodoItemNotFoundException(id))

        // When
        mvc
            .perform(
                put("$BASE_URL/$id/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isNotFound)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.error_code").value(ApiErrorCode.TODO_ITEM_NOT_FOUND.name))
    }

    @Test
    fun `should return conflict when todo item is PAST_DUE`() {
        // Given
        val id = UUID.randomUUID()
        val targetStatus = EditableTodoStatus.NOT_DONE
        val request = buildValidUpdateStatusRequest(targetStatus)
        whenever(service.updateStatus(id, targetStatus.toDomain())).thenThrow(
            TodoItemNotModifiableException(id, "status PAST_DUE doesn't allow changes"),
        )

        // When
        mvc
            .perform(
                put("$BASE_URL/$id/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.error_code").value(ApiErrorCode.TODO_ITEM_IMMUTABLE.name))
    }

    @Test
    fun `should return conflict when item was concurrently modified by status update`() {
        // Given
        val id = UUID.randomUUID()
        val request = buildValidUpdateStatusRequest(EditableTodoStatus.DONE)

        whenever(service.updateStatus(id, TodoItemStatus.DONE))
            .thenThrow(OptimisticLockingFailureException("Concurrent update"))

        // When
        mvc
            .perform(
                put("$BASE_URL/$id/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request),
            ) // Then
            .andExpect(status().isConflict)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.error_code").value(ApiErrorCode.CONCURRENT_MODIFICATION.name))
            .andExpect(jsonPath("$.timestamp").value(clock.instant().toString()))

        verify(service).updateStatus(id, TodoItemStatus.DONE)
    }

    @Test
    fun `should return all items when no status given`() {
        // Given
        val notDoneItem = createMockedResponse()
        val doneItem = createMockedResponse(status = TodoItemStatus.DONE, doneDateTime = clock.instant())
        val items = listOf(notDoneItem, doneItem)
        whenever(service.findAll(null)).thenReturn(items)

        // When
        mvc
            .perform(get(BASE_URL))
            // Then
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(notDoneItem.id.toString()))
            .andExpect(jsonPath("$[0].status").value("not done"))
            .andExpect(jsonPath("$[1].id").value(doneItem.id.toString()))
            .andExpect(jsonPath("$[1].status").value("done"))
    }

    @ParameterizedTest
    @CsvSource(
        "not done, NOT_DONE",
        "done, DONE",
        "past due, PAST_DUE",
    )
    fun `should return items matching status`(
        apiStatus: String,
        domainStatus: TodoItemStatus,
    ) {
        // Given
        val item =
            createMockedResponse(
                status = domainStatus,
                doneDateTime = if (domainStatus == TodoItemStatus.DONE) clock.instant() else null,
            )
        whenever(service.findAll(domainStatus)).thenReturn(listOf(item))

        // When
        mvc
            .perform(
                get(BASE_URL)
                    .param("status", apiStatus),
            ) // Then
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(item.id.toString()))
            .andExpect(jsonPath("$[0].status").value(apiStatus))
    }

    @Test
    fun `should return empty array when no items found matching status`() {
        // Given
        val status = TodoItemStatus.NOT_DONE
        whenever(service.findAll(status)).thenReturn(emptyList())

        // When & Then
        mvc
            .perform(
                get(BASE_URL)
                    .param("status", "not done"),
            ).andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().json("[]"))

        verify(service).findAll(status)
    }

    @Test
    fun `should return bad request when status filter is invalid`() {
        // Given
        val status = "unknown"

        // When
        mvc
            .perform(
                get(BASE_URL)
                    .param("status", status),
            ) // Then
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.error_code").value(ApiErrorCode.INVALID_REQUEST_PARAM.name))
            .andExpect(jsonPath("$.timestamp").value(clock.instant().toString()))

        verifyNoInteractions(service)
    }

    private fun buildValidCreateRequest() =
        """
        {
            "description": "testDescription",
            "due_datetime": "2039-09-09T09:09:09Z"
        }
        """.trimIndent()

    private fun createMockedResponse(
        id: UUID = UUID.randomUUID(),
        description: String = "testDescription",
        status: TodoItemStatus = TodoItemStatus.NOT_DONE,
        doneDateTime: Instant? = null,
    ) = TodoItem(
        id = id,
        description = description,
        creationDateTime = Instant.parse("2027-07-07T07:07:07Z"),
        dueDateTime = Instant.parse("2039-09-09T09:09:09Z"),
        status = status,
        doneDateTime = doneDateTime,
    )

    private fun buildCreateRequestWithBlankDescription() =
        """
        {
            "description": "",
            "due_datetime": "2039-09-09T09:09:09Z"
        }
        """.trimIndent()

    private fun buildCreateRequestWithTooLongDescription() =
        """
        {
            "description": "${generateLongString()}",
            "due_datetime": "2039-09-09T09:09:09Z"
        }
        """.trimIndent()

    private fun buildValidUpdateDescriptionRequest(newDescription: String) =
        """
        {
            "description": "$newDescription"
        }
        """.trimIndent()

    private fun buildUpdateDescriptionRequestBlank() =
        """
        {
            "description": ""
        }
        """.trimIndent()

    private fun buildUpdateDescriptionRequestTooLong() =
        """
        {
            "description": "${generateLongString()}"
        }
        """.trimIndent()

    private fun buildValidUpdateStatusRequest(targetStatus: EditableTodoStatus = EditableTodoStatus.NOT_DONE) =
        """
        {
            "status": "${targetStatus.value}"
        }
        """.trimIndent()

    companion object {
        const val BASE_URL = "/todo-list/v1/items"

        fun generateLongString() = "a".repeat(1001)
    }
}
