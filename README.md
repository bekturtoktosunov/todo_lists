# TRADEBYTE CODING CHALLENGE

## Service description

### Design Decisions

#### ID (UUID vs Long)

I chose **UUID** for ID as it is generated on the application side, and it keeps domain independent of the database.
The task didn't require performance considerations, so **Long** wasn't used.

#### Timestamp (Instant vs Offset-/ZonedDateTime)

I used **Instant** for date-time handling on every model layer for consistency and easy comparison.
The task didn't require handling time zones, so no **OffsetDateTime** or **ZonedDateTime** were used.

#### Lightweight State Machine

I implemented a small State Machine in TodoItemStatus. It checks if a target state of an item is reachable from the
current one (NOT_DONE, DONE, DUE_DATE).

#### Global Exception Handler

I decided to add GlobalExceptionHandler to have custom exception structure (Problem Detail with extra properties)
so the exceptions are more descriptive and to avoid stack traces in responses 

## Tech Stack

| Language                 | Framework         | Database | API Docs             | Payload format | Container | Tests             |
|--------------------------|-------------------|----------|----------------------|----------------|-----------|-------------------|
| Kotlin  2.3.21 (Java 25) | Spring Boot 4.1.1 | H2       | OpenApi + Swagger UI | JSON           | Docker    | JUnit 5 + Mockito |

## How-To Guides

### OpenApi / Swagger UI
You can reach the OpenApi documentation via web browser opening the following URL:
http://localhost:8080/swagger-ui/index.html

### API Quick Tests

You can test the API using provided ./request.http file in IntelliJ or using curl like in the example below.

```bash
curl --request POST 'http://localhost:8080/todo-list/v1/items' \
--header 'Content-Type: application/json' \
--data '{
"description": "Finish coding challenge",
"due_datetime": "2026-09-22T00:00:00Z"
}'
```

