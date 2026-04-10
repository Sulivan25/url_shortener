# URL Shortener Project

## Overview
A URL shortener service built with Java 17 and Spring Boot 3.3.5. Provides URL shortening, redirection, click analytics with time-series data, and Redis-based caching.

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.3.5
- **Build Tool**: Maven
- **Database**: PostgreSQL (production), H2 (development/testing)
- **Cache**: Redis
- **ORM**: Spring Data JPA / Hibernate

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
  controller/     - REST controllers (Analytics, Redirect, ShortUrlAdmin)
  domain/entity/  - JPA entities (ShortUrl, ClickDaily, ClickHourly)
  dto/            - Request/Response DTOs
  exception/      - Custom exceptions and global error handler
  infrastructure/ - Redis helpers
  repository/     - Spring Data JPA repositories
  scheduler/      - Scheduled jobs for click count sync
  service/        - Business logic (UrlShortenerService, AnalyticsService)
  util/           - Utilities (Base62 encoding)
```

## Coding Conventions
- Follow standard Java/Spring Boot conventions
- Use Lombok for boilerplate reduction
- Custom exceptions extend RuntimeException
- REST controllers use @RestController with proper HTTP methods
- Repository interfaces extend JpaRepository
- Use constructor injection (via Lombok @RequiredArgsConstructor)

## Branches
- `main` - production branch
- `develop` - integration branch
- Working branches use a `type/short-description` slash-style convention, all lowercase, kebab-case:
  - `feat/*` - new features (e.g. `feat/clickcount-analytics`)
  - `fix/*` - bug fixes (e.g. `fix/invalid-expiration-days`)
  - `test/*` - test-only changes (e.g. `test/analytics-service`)
  - `chore/*` - tooling, CI, docs, refactors (e.g. `chore/ci-workflow`)
