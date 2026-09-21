package com.tradebyte.challenge.todo_list.controller

import com.tradebyte.challenge.todo_list.config.TodoTestConfig
import com.tradebyte.challenge.todo_list.exception.TodoItemNotFoundException
import com.tradebyte.challenge.todo_list.model.domain.TodoItem
import com.tradebyte.challenge.todo_list.model.domain.TodoItemStatus
import com.tradebyte.challenge.todo_list.service.TodoService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@WebMvcTest(controllers = [TodoController::class])
@Import(TodoTestConfig::class)
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
            post(BASE_URL)
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
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ) // Then
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should throw exception when description is too long`() {
        // Given
        val request = createRequestWithTooLongDescription()

        // When
        mvc.perform(
            post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request)
        ) // Then
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should find todo list item`() {
        // Given
        val id = UUID.randomUUID()
        val mockedResponse = createMockedResponse(id)
        whenever(service.find(id)).thenReturn(mockedResponse)

        // When
        mvc.perform(
            get("$BASE_URL/$id")
        ) // Then
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id.toString()))
    }

    @Test
    fun `should throw exception when item not found`() {
        // Given
        val id = UUID.randomUUID()
        whenever(service.find(id)).thenThrow(TodoItemNotFoundException(id))

        // When
        mvc.perform(
            get("$BASE_URL/$id")
        ) // Then
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.detail").value("Todo list item with id $id not found"))
    }

    private fun createValidRequest() =
        """
            {
                "description": "testDescription",
                "due_datetime": "2039-09-09T09:09:09Z"
            }
        """.trimIndent()

    private fun createMockedResponse(id: UUID = UUID.randomUUID()) =
        TodoItem(
            id = id,
            description = "testDescription",
            creationDateTime = Instant.parse("2027-07-07T07:07:07Z"),
            dueDateTime = Instant.parse("2039-09-09T09:09:09Z"),
            status = TodoItemStatus.NOT_DONE
        )

    private fun createRequestWithBlankDescription() =
        """
            {
                "description": "",
                "due_datetime": "2039-09-09T09:09:09Z"
            }
        """.trimIndent()

    private fun createRequestWithTooLongDescription() =
        """
            {
                "description": "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor 
                invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo 
                duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit 
                amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt 
                ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores 
                et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. 
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut 
                labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores 
                et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.

                Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, 
                vel illum dolore eu feu",
                "dueDateTime": "2039-09-09T09:09:09Z"
            }
        """.trimIndent()

    companion object {
        const val BASE_URL = "/todo-list/v1/items"
    }
}