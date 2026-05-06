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
- [ ] Fetch Artist of the Day

## Production Readiness

- [ ] Validation
- [ ] Problem Details-style errors
- [ ] Actuator health
- [ ] OpenAPI docs
- [ ] Tests with Testcontainers
- [ ] Dockerfile
- [ ] README run instructions

## Final

- [ ] Review docs
- [ ] Run tests
- [ ] Run app locally
- [ ] Run with Docker Compose
- [ ] Final cleanup

## Reminder

- [x] Normalise blank genre to null
- [x] Validate positive track length at API level
- [x] Normalise ISRC to uppercase
- [x] Return duplicate ISRC as 409 Conflict
