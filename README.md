# TRADEBYTE CODING CHALLENGE

## Service description

### Design Decisions

#### Separate API, Domain, and Persistence Models

I keep request/response DTOs, domain objects, and JPA entities separate so API changes and persistence details do not
directly affect each other.
I used explicit mappings to make conversions visible, accepting some additional boilerplate.

#### ID (UUID vs Long)

I chose **UUID** for ID as it is generated on the application side, and it keeps domain independent of the database.

#### Timestamp (Instant vs Offset-/ZonedDateTime)

I used **Instant** for date-time handling on every model layer for consistency and easy comparison.
The task didn't require handling time zones, so no **OffsetDateTime** or **ZonedDateTime** were used.

#### Injectable Clock

I used an injected Clock for timestamps and due-date validation. Production uses a UTC clock, while tests use a fixed
clock to make time-dependent behavior deterministic.

#### Lightweight State Machine

I implemented a small State Machine in TodoItemStatus. It checks if a target state of an item is reachable from the
current one (NOT_DONE, DONE, PAST_DUE).

#### Status Updates via PUT

I use PUT /todo-list/v1/items/{id}/status to update an item's status, treating it as a subresource.
The request accepts only "done" and "not done"; "past due" is managed by the service.

The operation is idempotent: requesting the current status leaves the item unchanged.
Marking an item as done sets done_datetime using the injected Clock; marking it as not done clears it.
Items with status "past due" cannot be modified.

#### Item Listing and Status Filtering

I use GET /todo-list/v1/items with optional status query parameter. Without the parameter, all items are returned.

Filtering and sorting are performed by the database. Items are ordered by creation_datetime and id to keep the order
deterministic. When no items found empty array is returned with HTTP 200.

#### Global Exception Handler

I decided to add GlobalExceptionHandler to have custom exception structure (Problem Detail with extra properties)
so the exceptions are more descriptive and consistent.

#### Transactional Updates

I update items within a service-level transaction, loading the entity, modifying it, and letting JPA dirty checking
persist the change. This also leaves room for checking business rules before applying updates.

#### Optimistic Locking

Todo items use JPA optimistic locking via the @Version field to prevent lost updates when the same item is modified
concurrently.

#### Automatic Expiration

I use a scheduled task to mark "not done" items with expired dueDateTime as "past due". The delay between executions is
configurable via todo.expiration.delay-ms. Items are bulk updated within a transaction incrementing version
to prevent concurrent updates.

Read responses may temporarily show previous status until scheduled task runs. API update operations also check the
due_datetime. Items marked as "done" do not expire. If an expired done item is marked as "not done", it expires on the
next scheduled execution.

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
"due_datetime": "2036-09-22T00:00:00Z"
}'
```

