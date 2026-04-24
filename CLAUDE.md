# URL Shortener Project

## Overview
A URL shortener service built with Java 17 and Spring Boot 3.3.5. Provides URL shortening, redirection, click analytics with time-series data, Redis-based caching, JWT authentication, and role-based admin management.

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.3.5
- **Build Tool**: Maven
- **Database**: PostgreSQL (production), H2 (development/testing)
- **Cache**: Redis
- **ORM**: Spring Data JPA / Hibernate
- **Security**: Spring Security 6 + JWT (jjwt 0.12.6)
- **Testing**: JUnit 5, Mockito, MockMvc, spring-security-test

## Build & Run Commands
```bash
# Build the project
mvn clean install

# Run tests
mvn test

# Run the application
mvn spring-boot:run
```

## Project Structure
```
src/main/java/org/example/urlshortener/
  admin/          - Admin-only controllers + DTOs (AdminUrlController, AdminUserController)
  auth/           - Auth controller, AuthService, BootstrapAdminRunner, auth DTOs
  controller/     - REST controllers (Analytics, Redirect, ShortUrl user-facing CRUD)
  domain/entity/  - JPA entities (ShortUrl, ClickDaily, ClickHourly)
  dto/            - Request/Response DTOs
  exception/      - Custom exceptions and global error handler
  infrastructure/ - Redis helpers
  repository/     - Spring Data JPA repositories
  scheduler/      - Scheduled jobs for click count sync
  security/       - SecurityConfig, JwtService, JwtAuthenticationFilter, OwnershipService
  service/        - Business logic (UrlShortenerService, AnalyticsService)
  user/           - User entity, Role enum, UserRepository
  util/           - Utilities (Base62 encoding)

src/test/java/org/example/urlshortener/
  integration/    - Integration tests (@SpringBootTest + MockMvc + H2)
  domain/entity/  - Unit tests for entities
  service/        - Unit tests for services (mocked)
  infrastructure/ - Unit tests for Redis helpers
  util/           - Unit tests for utilities
```

## Coding Conventions
- Follow standard Java/Spring Boot conventions
- Use Lombok for boilerplate reduction
- Custom exceptions extend RuntimeException
- REST controllers use @RestController with proper HTTP methods
- Repository interfaces extend JpaRepository
- Use constructor injection (via Lombok @RequiredArgsConstructor)
- Use CLAUDE.md as the only source of persistent context for this project. Do not write to the file-based memory directory.

## Branches
- `main` - production branch
- `develop` - integration branch
- Working branches use a `type/short-description` slash-style convention, all lowercase, kebab-case:
  - `feat/*` - new features (e.g. `feat/clickcount-analytics`)
  - `fix/*` - bug fixes (e.g. `fix/invalid-expiration-days`)
  - `test/*` - test-only changes (e.g. `test/analytics-service`)
  - `chore/*` - tooling, CI, docs, refactors (e.g. `chore/ci-workflow`)

## Known Issues & Fixes (feat/auth-and-admin-service)

### shortCode NOT NULL bug
- **Problem**: `ShortUrl.shortCode` column is NOT NULL + UNIQUE, but the old `createShortUrl()` passed `null` on first save (before the DB-assigned ID existed to generate the Base62 code).
- **Why tests missed it**: Unit tests mock the repository — Mockito doesn't enforce DB constraints. Only a real DB (H2/Postgres) rejects the NULL insert.
- **Fix**: Use a UUID placeholder (`"tmp-" + UUID.randomUUID()...`) on first save, then replace with real Base62 code on second save.

### /error endpoint 401 masking
- **Problem**: Spring Boot's internal `/error` dispatcher was behind the auth filter, so any controller exception got masked as a 401 instead of the real error.
- **Fix**: Added `.requestMatchers("/error").permitAll()` in SecurityConfig.

## Integration Tests
- Location: `src/test/java/org/example/urlshortener/integration/`
- Base class: `BaseIntegrationTest` — boots full Spring context with H2 (PostgreSQL mode), mocks Redis via `@MockBean`
- Profile: `application-integration.yml` — H2 create-drop, Redis auto-config disabled
- Uses `@DirtiesContext(AFTER_EACH_TEST_METHOD)` for clean DB per test
- **31 tests** across 6 files covering: Auth, Short URL CRUD, Redirect, Analytics, Admin URL, Admin User
- Note: Spring Boot 3.3.5 uses `@MockBean` (not `@MockitoBean` which requires 3.4+)

## Docker
- **Dockerfile**: Multi-stage build — `eclipse-temurin:17-jdk` (build) → `eclipse-temurin:17-jre` (runtime)
  - Note: Alpine variants (`-alpine`) don't support Apple Silicon (ARM), use non-alpine images
- **docker-compose.yml**: 3 services — app, postgres (16-alpine), redis (7-alpine)
  - Healthchecks on postgres/redis, app waits via `depends_on: condition: service_healthy`
  - Persistent volume `pgdata` for PostgreSQL data
- **application-docker.yml**: Profile for Docker — PostgreSQL connection, reads secrets from env vars
- **.dockerignore**: Excludes target/, .idea/, .git/
- **Verified working** (2026-04-12): register, create URL, redirect, admin endpoints all tested successfully

## Feature Roadmap
Potential features to add (not yet started):
- **Rate Limiting** — per-user/IP via Redis sliding window
- **QR Code Generation** — auto-generate QR for each short URL
- **Custom Short Code** — vanity URLs chosen by user
- **Geo/Device Analytics** — click tracking by country/device via IP geolocation + User-Agent parsing
- **Webhook Notifications** — notify user when link hits N clicks
