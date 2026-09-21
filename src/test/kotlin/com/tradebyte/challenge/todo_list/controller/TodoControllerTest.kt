package com.tradebyte.challenge.todo_list.controller

import com.tradebyte.challenge.todo_list.model.domain.TodoItem
import com.tradebyte.challenge.todo_list.model.domain.TodoItemStatus
import com.tradebyte.challenge.todo_list.service.TodoService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@WebMvcTest(controllers = [TodoController::class])
class TodoControllerTest {
    @Autowired
    lateinit var mvc: MockMvc

    @MockitoBean
    lateinit var service: TodoService

    @Test
    fun `should create todo list item`() {
        // Given
        val request = createValidRequest()
        val mockedResponse = createMockedResponse()
        whenever(service.create(any())).thenReturn(mockedResponse)

        // When
        mvc.perform(
            post("/todo-list/v1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ) // Then
            .andExpect(status().isCreated)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.description").value("testDescription"))
    }

    @Test
    fun `should throw exception when description is blank`() {
        // Given
        val request = createRequestWithBlankDescription()

        // When
        mvc.perform(
            post("/todo-list/v1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ) // Then
            .andExpect(status().isBadRequest)
    }

    private fun createValidRequest() =
        """
            {
                "description": "testDescription",
                "dueDateTime": "2039-09-09T09:09:09Z"
            }
        """.trimIndent()

    private fun createMockedResponse() =
        TodoItem(
            id = UUID.randomUUID(),
            description = "testDescription",
            creationDateTime = Instant.parse("2027-07-07T07:07:07Z"),
            dueDateTime = Instant.parse("2039-09-09T09:09:09Z"),
            status = TodoItemStatus.NOT_DONE
        )

    private fun createRequestWithBlankDescription() =
        """
            {
                "description": "",
                "dueDateTime": "2039-09-09T09:09:09Z"
            }
        """.trimIndent()
}