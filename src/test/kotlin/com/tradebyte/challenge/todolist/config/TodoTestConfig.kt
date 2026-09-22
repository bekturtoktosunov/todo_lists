package com.tradebyte.challenge.todolist.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@TestConfiguration
class TodoTestConfig {
    @Bean
    fun clock(): Clock = Clock.fixed(Instant.parse("2026-09-20T00:00:00Z"), ZoneOffset.UTC)
}
