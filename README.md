# Tasks API

[![Backend CI](https://github.com/a-mikh/tasks/actions/workflows/ci.yml/badge.svg)](https://github.com/a-mikh/tasks/actions/workflows/ci.yml)

## Overview

Tasks API is a Spring Boot REST API for an authenticated shared task board. Users can register, sign in, create and assign tasks, move tasks through their workflow, and retrieve filtered, paginated task results.

The application uses stateless bearer authentication. A successful login returns a short-lived JWT access token, and every task endpoint requires a valid token. All authenticated users currently work with the same shared board; task ownership and roles are outside the current scope.

Frontend repository: [a-mikh/tasks-front](https://github.com/a-mikh/tasks-front)

## Features

- Register users with unique usernames and securely encoded passwords
- Authenticate users and issue signed HS256 JWT access tokens
- Protect task endpoints with stateless Spring Security authentication
- Configure the access-token lifetime through application configuration
- Create tasks with optional descriptions
- Assign and reassign registered users to tasks
- Advance task status through `TODO -> IN_PROGRESS -> DONE`
- Prevent invalid status transitions
- Filter tasks by status and assignee
- Paginate task results with stable ordering
- Validate request fields, character rules, and storage limits
- Return consistent structured error responses
- Manage schema changes with versioned Flyway migrations

## Tech Stack

- Java 25
- Spring Boot 4.1
- Spring Web
- Spring Security
- Spring Security OAuth2 Resource Server and JOSE
- Spring Data JPA / Hibernate
- PostgreSQL 17
- Flyway
- Bean Validation
- Gradle
- JUnit 5 and Spring Security Test
- Testcontainers
- Docker / Docker Compose
- GitHub Actions

## Security Model

`POST /auth/register` and `POST /auth/login` are public. Every other request must include a valid access token:

```http
Authorization: Bearer <access-token>
```

Passwords are processed through Spring Security's delegating `PasswordEncoder` and are never stored as plain text. JWTs are signed with HS256 using a Base64-encoded secret of at least 32 bytes. The token contains the user ID as its subject and includes issued-at, expiration, issuer, and audience claims.

The API uses Spring Security OAuth2 Resource Server components to decode and authenticate bearer JWTs. It does not implement an OAuth 2.0 authorization flow or a standalone authorization server.

Authentication answers who is making a request. The current shared-board model gives every authenticated user access to all tasks, so ownership rules and role-based authorization are not implemented yet.

## Running Locally

### Prerequisites

- Docker with Docker Compose
- Git

### Start the full backend stack

1. Create a local environment file:

```bash
cp .env.example .env
```

2. Set database credentials and generate a JWT signing secret:

```bash
openssl rand -base64 32
```

Copy the generated value into `.env`:

```env
DB_USERNAME=tasks
DB_PASSWORD=change-me
JWT_SECRET_BASE64=generated-base64-value
```

3. Start PostgreSQL and the application:

```bash
docker compose up --build
```

The API is available at `http://localhost:8080`.

To stop the containers:

```bash
docker compose down
```

To also remove the PostgreSQL data volume:

```bash
docker compose down -v
```

The real `.env` file and signing secret must not be committed.

### Start the frontend

With the backend running, start the Angular application in a separate terminal:

```bash
git clone https://github.com/a-mikh/tasks-front.git
cd tasks-front
npm ci
npm start
```

Open `http://localhost:4200`. The Angular development server forwards `/auth` and `/tasks` requests to the backend through its development proxy.

## Configuration

| Property                     | Environment value used by Docker Compose | Purpose                                    |
| ---------------------------- | ---------------------------------------- | ------------------------------------------ |
| `spring.datasource.username` | `DB_USERNAME`                            | PostgreSQL username                        |
| `spring.datasource.password` | `DB_PASSWORD`                            | PostgreSQL password                        |
| `app.jwt.secret-base64`      | `JWT_SECRET_BASE64`                      | Base64-encoded HS256 signing key           |
| `app.jwt.access-token-ttl`   | Default: `15m`                           | Access-token lifetime as a Java `Duration` |

Spring Boot configuration can be overridden through its standard property sources when the application is run outside Docker Compose.

## API Overview

| Access        | Method  | Endpoint                            | Description                              |
| ------------- | ------- | ----------------------------------- | ---------------------------------------- |
| Public        | `POST`  | `/auth/register`                    | Register a user                          |
| Public        | `POST`  | `/auth/login`                       | Authenticate and receive an access token |
| Authenticated | `POST`  | `/tasks`                            | Create a task                            |
| Authenticated | `PUT`   | `/tasks/{taskId}/assign/{username}` | Assign or reassign a registered user     |
| Authenticated | `PATCH` | `/tasks/{id}/status/next`           | Advance task status                      |
| Authenticated | `GET`   | `/tasks`                            | Retrieve filtered and paginated tasks    |
| Authenticated | `GET`   | `/tasks/{id}`                       | Retrieve one task                        |

`GET /tasks` supports these query parameters:

- `status`: `TODO`, `IN_PROGRESS`, or `DONE`
- `assignee`: exact username
- `page`: zero-based page number
- `size`: number of tasks per page; the backend default is `20`

Example login response:

```json
{
  "accessToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresInSeconds": 900
}
```

## Error Handling

The API uses centralized exception handling and a consistent error structure.

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
  "path": "/auth/register",
  "fieldErrors": {
    "username": "must not be blank"
  }
}
```

Invalid login credentials return the same generic response whether the username is unknown or the password is incorrect, avoiding unnecessary account disclosure.

## Testing

Run the complete test suite with:

```bash
./gradlew clean test
```

Integration tests use Testcontainers to start a temporary PostgreSQL instance, so Docker must be running. The suite covers authentication, JWT claims, protected endpoints, persistence, validation, structured errors, pagination, filtering, task status transitions, and database constraint handling.

## Database Migrations

Database schema changes are managed with Flyway. Hibernate uses `ddl-auto=validate`, so it validates the migrated schema without modifying it.

Migration files are located in:

```text
src/main/resources/db/migration
```

## Project Structure

```text
controller   REST endpoints
service      application and authentication logic
repository   persistence access
model        JPA entities and domain state
dto          request and response contracts
security     Spring Security, password encoding, and JWT configuration
error        centralized API error handling
exceptions   application-specific exceptions
```

## Future Improvements

- Add task ownership and per-user task views
- Add roles and method-level authorization for administrative operations
- Introduce refresh-token rotation and revocation if longer-lived sessions are required
- Validate JWT issuer and audience explicitly
- Consider asymmetric signing or an external identity provider for multi-service deployment
- Add login rate limiting, password reset, and email verification
- Add OpenAPI / Swagger documentation
- Add optimistic locking for concurrent task updates
