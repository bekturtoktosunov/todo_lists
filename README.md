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

#### Docker Build

I use a multi-stage Docker build: JDK 25 compiles the application using the Gradle Wrapper, and JRE 25 runs the
resulting executable JAR.

The runtime image does not include source files or build tools. The application runs as a non-root user.

## Tech Stack

| Language                 | Framework         | Database | API Docs             | Payload format | Container | Tests             |
|--------------------------|-------------------|----------|----------------------|----------------|-----------|-------------------|
| Kotlin  2.3.21 (Java 25) | Spring Boot 4.1.1 | H2       | OpenApi + Swagger UI | JSON           | Docker    | JUnit 5 + Mockito |

## How-To Guides

### Build service

### Run automatic tests

### Run using Docker

Docker must be installed and running. On Windows, use Docker Desktop in Linux containers mode. A local Java or Gradle
installation is not required.

Run the following commands from the project root.

1. Build the image:

```bash
docker build -t todo-list:local .
```

The image is built from source using the Gradle Wrapper. The first build requires internet access to download base
images and dependencies. Tests are not executed during the Docker build.

2. Start the container:

```bash
docker run --rm --name todo-list -p 127.0.0.1:8080:8080 todo-list:local
```

The API is available at http://localhost:8080/todo-list/v1/items.
Swagger UI is available at http://localhost:8080/swagger-ui/index.html.

If port 8080 is already in use, map a different local port:

```bash
docker run --rm --name todo-list -p 127.0.0.1:8081:8080 todo-list:local
```

In this case, use port 8081 in the URLs above.

3. Stop the container from another terminal:

```bash
docker stop todo-list
```

The service uses an in-memory H2 database. All data is lost when the application stops. The container is automatically
removed after stopping, but the image remains available.

### Check OpenApi / Swagger UI

Swagger UI is available at http://localhost:8080/swagger-ui/index.html.

### Run API Quick Tests

You can test the API using provided ./request.http file in IntelliJ or using curl like in the example below.

```bash
curl --request POST 'http://localhost:8080/todo-list/v1/items' \
--header 'Content-Type: application/json' \
--data '{
"description": "Finish coding challenge",
"due_datetime": "2036-09-22T00:00:00Z"
}'
```

