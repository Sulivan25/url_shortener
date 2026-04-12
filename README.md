# URL Shortener

A RESTful URL shortener service with JWT authentication, role-based access control, click analytics (hourly/daily time-series), and Redis caching.

Built with Java 17, Spring Boot 3.3.5, PostgreSQL, and Redis.

## Features

- **URL Shortening** — shorten any URL with auto-generated Base62 short codes
- **Redirect** — `GET /{shortCode}` redirects to the original URL (302)
- **Link Expiration** — optional TTL in days, extendable by owner or admin
- **JWT Authentication** — register/login, stateless Bearer token auth
- **Role-Based Access** — `USER` owns their links, `ADMIN` manages everything
- **Ownership Enforcement** — users can only view/edit/delete their own URLs
- **Click Analytics** — hourly and daily click count time-series per URL
- **Redis Caching** — redirect lookups cached in Redis, click counters synced to DB via scheduled jobs
- **Admin Panel API** — manage all URLs and users (ADMIN role required)

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security 6 + JWT (jjwt 0.12.6) |
| Database | PostgreSQL 16 (production), H2 (development) |
| Cache | Redis 7 |
| ORM | Spring Data JPA / Hibernate |
| Build | Maven |
| Containerization | Docker + Docker Compose |
| Testing | JUnit 5, Mockito, MockMvc, spring-security-test |

## Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (for containerized setup)

## Getting Started

### Option 1: Docker Compose (recommended)

```bash
# Clone the repository
git clone https://github.com/Sulivan25/url-shortener.git
cd url-shortener

# Start all services (app + PostgreSQL + Redis)
docker compose up --build
```

App sẽ chạy tại `http://localhost:8080`.

Để override secrets, tạo file `.env`:
```env
JWT_SECRET=your-production-secret-here
BOOTSTRAP_ADMIN_PASSWORD=your-secure-admin-password
```

### Option 2: Local Development

```bash
# Start Redis locally (required for caching)
redis-server

# Run with H2 in-memory database (default profile)
mvn spring-boot:run

# Or run with PostgreSQL (docker profile)
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

### Run Tests

```bash
# All tests (unit + integration)
mvn test

# Only integration tests
mvn test -Dtest="org.example.urlshortener.integration.*"
```

## API Endpoints

### Public

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Login, returns JWT |
| `GET` | `/{shortCode}` | Redirect to original URL |

### Authenticated (Bearer token required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/short-urls` | Create a short URL |
| `GET` | `/api/short-urls/me` | List your own URLs (paginated) |
| `POST` | `/api/short-urls/{shortCode}/extend` | Extend expiration (owner or admin) |
| `DELETE` | `/api/short-urls/{shortCode}` | Delete a short URL (owner or admin) |
| `GET` | `/api/short-urls/{shortCode}/analytics/hourly` | Hourly click analytics (owner or admin) |

### Admin Only (ADMIN role required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/admin/short-urls` | List all URLs (paginated) |
| `POST` | `/admin/short-urls/{shortCode}/extend` | Extend any URL |
| `DELETE` | `/admin/short-urls/{shortCode}` | Delete any URL |
| `GET` | `/admin/short-urls/{shortCode}/analytics/hourly` | Analytics for any URL |
| `GET` | `/admin/users` | List all users (paginated) |
| `POST` | `/admin/users` | Create a user with role |
| `PATCH` | `/admin/users/{id}/role` | Change user role |
| `DELETE` | `/admin/users/{id}` | Delete a user |

## Usage Examples

### Register
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"password123"}'
```
```json
{"token":"eyJhbGci...","username":"alice","role":"USER"}
```

### Create Short URL
```bash
curl -X POST http://localhost:8080/api/short-urls \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://example.com","expireDays":30}'
```
```json
{"shortCode":"b","originalUrl":"https://example.com","createdAt":"...","expireAt":"...","clickCount":0,"owner":"alice"}
```

### Redirect
```bash
curl -L http://localhost:8080/b
# -> redirects to https://example.com
```

## Project Structure

```
src/main/java/org/example/urlshortener/
  admin/          - Admin-only controllers + DTOs
  auth/           - AuthController, AuthService, BootstrapAdminRunner
  controller/     - ShortUrlController, RedirectController, AnalyticsController
  domain/entity/  - JPA entities (ShortUrl, ClickDaily, ClickHourly)
  dto/            - Request/Response DTOs
  exception/      - Custom exceptions + GlobalExceptionHandler
  infrastructure/ - Redis helpers
  repository/     - Spring Data JPA repositories
  scheduler/      - Scheduled click count sync jobs
  security/       - SecurityConfig, JWT filter, OwnershipService
  service/        - UrlShortenerService, AnalyticsService
  user/           - User entity, Role enum, UserRepository
  util/           - Base62 encoding utility
```

## Testing

The project has two layers of tests:

**Unit Tests** — mocked dependencies, fast, no Spring context:
- Entity behavior (ShortUrl, expiration logic)
- Service logic (UrlShortenerService, AnalyticsService)
- Utilities (Base62, RedisKeyHelper)

**Integration Tests** (31 tests) — full Spring context + H2 database:
- Auth: register, login, validation, duplicate username
- Short URL CRUD: create, list, extend, delete, ownership checks
- Redirect: 302, expired (410), not found (404)
- Analytics: owner access, non-owner blocked, admin bypass
- Admin URL: list all, extend/delete any, role enforcement
- Admin User: list, create, change role, delete, access control

## Default Credentials

On first startup, a bootstrap admin is created:
- **Username**: `admin`
- **Password**: `admin123!`

Change these via environment variables `BOOTSTRAP_ADMIN_USERNAME` and `BOOTSTRAP_ADMIN_PASSWORD`.
