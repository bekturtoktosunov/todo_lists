package com.tradebyte.challenge.todo_list.service

import com.tradebyte.challenge.todo_list.repository.TodoJpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class TodoExpirationService(
    private val repository: TodoJpaRepository,
    private val clock: Clock
) {

    @Transactional
    fun markItemsPastDue() {
        repository.markItemsPastDue(now = clock.instant())
    }

}
