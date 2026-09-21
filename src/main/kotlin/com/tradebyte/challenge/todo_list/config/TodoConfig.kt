package com.tradebyte.challenge.todo_list.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class TodoConfig {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}