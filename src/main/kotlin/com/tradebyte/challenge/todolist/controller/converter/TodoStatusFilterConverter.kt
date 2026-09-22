package com.tradebyte.challenge.todolist.controller.converter

import com.tradebyte.challenge.todolist.model.dto.request.TodoStatusFilter
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class TodoStatusFilterConverter : Converter<String, TodoStatusFilter> {
    override fun convert(source: String): TodoStatusFilter = TodoStatusFilter.entries.find { it.value == source }
        ?: throw IllegalArgumentException("Unknown status: $source")
}
