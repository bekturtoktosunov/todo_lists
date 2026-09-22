package com.tradebyte.challenge.todolist.scheduler

import com.tradebyte.challenge.todolist.service.TodoExpirationService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class TodoExpirationScheduler(
    private val expirationService: TodoExpirationService,
) {
    @Scheduled(fixedDelayString = "\${todo.expiration.delay-ms:10000}")
    fun scheduleExpiration() {
        expirationService.markItemsPastDue()
    }
}
