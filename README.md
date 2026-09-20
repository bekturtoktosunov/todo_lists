# TRADEBYTE CODING CHALLENGE

## Service description

### Design Decisions

#### ID (UUID vs Long)

I chose **UUID** for ID as it is generated on the application side, and it keeps domain independent of the database.
The task didn't require performance considerations, so **Long** wasn't used.

#### Timestamp (Instant vs Offset-/ZonedDateTime)

I used **Instant** for date-time handling on every model layer for consistency and easy comparison.
The task didn't require handling time zones, so no **OffsetDateTime** or **ZonedDateTime** were used.

## Tech Stack

| Language                 | Framework         | Database | API Docs             | Payload format | Container | Tests   |
|--------------------------|-------------------|----------|----------------------|----------------|-----------|---------|
| Kotlin  2.3.21 (Java 25) | Spring Boot 4.1.1 | H2       | OpenApi + Swagger UI | JSON           | Docker    | JUnit 5 |

## How-To Guides
