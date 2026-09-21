package com.tradebyte.challenge.todo_list.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@OpenAPIDefinition(
    info = Info(
        title = "Todo List API",
        version = "1.0",
        description = "Simple todo list service"
    )
)
@Configuration
class TodoConfig {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}