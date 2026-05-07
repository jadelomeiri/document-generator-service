# TODO

## Foundation

- [x] Confirm Spring Boot project builds
- [x] Configure PostgreSQL connection
- [x] Add Docker Compose for local Postgres
- [x] Configure Flyway

## Domain

- [x] Add Artist entity
- [x] Add ArtistAlias entity
- [x] Add Track entity
- [x] Add Flyway migration
- [x] Add repositories

## API

- [x] Create artist
- [x] Get artist
- [x] Update artist primary name
- [x] Add artist alias
- [x] List artist aliases
- [x] Add track to artist
- [x] Fetch artist tracks with pagination
- [x] Fetch Artist of the Day

## Production Readiness

- [x] Validation
- [x] Problem Details-style errors
- [x] Actuator health and probes
- [x] Prometheus metrics endpoint
- [x] OpenAPI / Swagger docs
- [x] Tests with Testcontainers
- [x] Dockerfile
- [x] Docker Compose app + database support
- [x] GitHub Actions CI
- [x] Lightweight Checkstyle
- [x] Dependabot
- [x] Environment-specific configuration
- [x] README run instructions

## Final

- [ ] Final documentation review
- [x] Run tests
- [x] Run app locally with `SPRING_PROFILES_ACTIVE=local`
- [x] Run PostgreSQL locally with Docker Compose
- [ ] Run app and PostgreSQL together with Docker Compose
- [x] Verify Actuator health/readiness/liveness/prometheus endpoints
- [ ] Final cleanup

## Reminder

- [x] Normalise blank genre to null
- [x] Validate positive track length at API level
- [x] Normalise ISRC to uppercase
- [x] Return duplicate ISRC as 409 Conflict
- [ ] Consider renaming `GET /api/v1/homepage/artist-of-the-day`

## Notes

- Docker image builds use `./gradlew clean bootJar --no-daemon` rather than running tests because CI owns the full `./gradlew clean build --no-daemon` gate, including Testcontainers-backed tests.
