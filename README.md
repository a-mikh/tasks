# Tasks API

## Overview

Tasks API is a backend service for managing users and tasks.
The project supports task creation, assignment, status transitions, filtering and pagination.
It was built as an independent Spring Boot project with a focus on business rules, API design, persistence, testing and reproducible local development.

## Features

- Create users with unique usernames
- Create tasks with optional descriptions
- Assign and reassign existing users to tasks
- Advance task status through `TODO -> IN_PROGRESS -> DONE`
- Prevent invalid status transitions
- Filter tasks by status and assignee
- Paginate task results with stable ordering
- Validate request fields and length constraints
- Return consistent structured error responses

## Tech Stack

- Java 25
- Spring Boot 4.1
- Spring Web
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway
- Bean Validation
- Gradle
- JUnit 5
- Testcontainers
- Docker / Docker Compose
- GitHub Actions

## Running Locally

### Prerequisites

- Docker
- Docker Compose

### Start the application

1. Create a local environment file:

```bash
cp .env.example .env
```

2. Set database credentials in `.env`.

3. Start the application:
```bash

docker compose up --build
```
The API will be available at:

`http://localhost:8080`

To stop the application:
```bash
docker compose down
```

To stop the application and remove the PostgreSQL data volume:
```bash
docker compose down -v
```

## Configuration

Local database credentials are configured through `.env`.

Example:

```env
DB_USERNAME=tasks
DB_PASSWORD=change-me
```

Use `.env.example` as the template. The real `.env` file is not committed to Git.

## API Overview

| Method | Endpoint                            | Description                                |
|--------|-------------------------------------|--------------------------------------------|
| POST   | `/users`                            | Create a user                              |
| POST   | `/tasks`                            | Create a task                              |
| PUT    | `/tasks/{taskId}/assign/{username}` | Assign or reassign a user                  |
| PATCH  | `/tasks/{id}/status/next`           | Advance task status                        |
| GET    | `/tasks`                            | Retrieve tasks with pagination and filters |

Supported filters for `GET /tasks`:

- `status`
- `assignee`
- standard pagination parameters such as `page` and `size`

## Error Handling

The API uses a centralized exception handler and returns a consistent error structure.

Example:

```json
{
  "status": 404,
  "code": "TASK_NOT_FOUND",
  "message": "Task with id 999 not found.",
  "path": "/tasks/999/status/next",
  "fieldErrors": {}
}
```

Validation errors include field-specific details:
```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/users",
  "fieldErrors": {
    "username": "must not be blank"
  }
}
```
## Testing

Run the complete test suite with:

```bash
./gradlew clean test
```

Integration tests use Testcontainers to start a temporary PostgreSQL instance, so they do not depend on a locally installed database. Docker must be running when the integration tests are executed.
The test suite covers API behavior, persistence, validation, error responses, pagination, task status transitions and database constraint handling.

## Database Migrations

Database schema changes are managed with Flyway.

Hibernate is configured with `ddl-auto=validate`, so Hibernate validates the schema but does not modify it automatically.

Migration files are located in:

```text
src/main/resources/db/migration
```

## Project Structure

```text
controller   HTTP endpoints
service      application logic
repository   persistence access
model        JPA entities and domain state
dto          request and response models
error        centralized API error handling
exceptions   application-specific exceptions
```

## Future Improvements

- Add OpenAPI / Swagger documentation
- Add optimistic locking for concurrent task updates
- Introduce authentication and authorization if the application scope requires it
- Add custom pagination response DTO instead of exposing Spring `Page`
