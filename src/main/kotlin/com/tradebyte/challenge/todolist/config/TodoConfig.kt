package com.tradebyte.challenge.todolist.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class TodoConfig {
    @Bean
    fun clock(): Clock = Clock.systemUTC()
}
