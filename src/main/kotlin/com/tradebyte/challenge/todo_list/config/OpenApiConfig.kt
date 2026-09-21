package com.tradebyte.challenge.todo_list.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MapSchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.ObjectSchema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    info = Info(
        title = "Todo List API",
        version = "1.0",
        description = "Simple todo list service"
    )
)
@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI {
        val problemDetailSchema = problemDetailSchema()
        return OpenAPI().components(
            Components()
                .addResponses(
                    "BadRequest",
                    ApiResponse()
                        .description("Validation error")
                        .content(
                            Content().addMediaType(
                                "application/problem+json",
                                MediaType().schema(problemDetailSchema)
                            )
                        )

                )
                .addResponses(
                    "NotFound",
                    ApiResponse()
                        .description("Todo item not found")
                        .content(
                            Content().addMediaType(
                                "application/problem+json",
                                MediaType().schema(problemDetailSchema)
                            )
                        )
                )
        )
    }

    companion object {
        private fun problemDetailSchema() =
            ObjectSchema()
                .addProperty("type", StringSchema().format("uri"))
                .addProperty("title", StringSchema())
                .addProperty("status", IntegerSchema().format("int32"))
                .addProperty("detail", StringSchema())
                .addProperty("instance", StringSchema().format("uri"))
                .addProperty("timestamp", StringSchema().format("date-time"))
                .addProperty(
                    "errors",
                    MapSchema().additionalProperties(StringSchema())
                )
    }
}