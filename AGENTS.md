# AGENTS.md

## Project

This is a production-minded JVM take-home task for a Tech Lead role at ICE.

Build a Music Metadata Service for a streaming-platform-like product serving many users globally.

The implementation should stay pragmatic: not toy CRUD, but not over-engineered platform theatre.

## Stack

- Java 25
- Spring Boot 4
- PostgreSQL
- Flyway
- Spring Web
- Spring HATEOAS
- Spring Data JPA
- Jakarta Validation
- Spring Boot Actuator
- SpringDoc OpenAPI
- JUnit 5
- Testcontainers
- Docker Compose for local/runtime support

## Engineering Standards

- Keep the solution intentionally scoped and production-minded.
- Prefer simple, readable code over clever abstractions.
- Use UUIDs for stable entity identity.
- Do not expose JPA entities directly from controllers.
- Use request/response DTOs or records for API contracts.
- Validate request DTOs with Jakarta Validation.
- Use clean error handling with Problem Details-style responses.
- Use Flyway migrations, not Hibernate auto-DDL.
- Add meaningful tests for business logic and API behaviour.
- Keep README and docs updated as implementation decisions change.
- When documenting decisions, include alternatives considered and why they were not chosen.
- Avoid generic enterprise phrasing. Prefer clear, pragmatic reasoning.

## Domain Rules

- Artist has stable identity and a primary display name.
- Artist aliases must be modelled explicitly.
- Artist aliases should be treated as realistic metadata, not just a string field.
- Some artists may have many aliases, so aliases should be stored as separate records.
- Aliases do not count as separate artists for Artist of the Day.
- Tracks belong to one artist.
- Some artists may have very large catalogues, so track retrieval must avoid unbounded responses.
- Fetching tracks must be paginated.
- Artist of the Day must be deterministic, fair, and cyclical.
- Use UTC date and an injectable Clock for testability.
- Keep the model focused on artists, aliases, and tracks. Do not expand into full copyright/work/rightsholder modelling unless explicitly requested.

## Prioritisation

Use `docs/PRODUCTION_READINESS.md` to prioritise work.

Complete P0 before implementing P1.

Do not implement P2 items unless explicitly asked. P2 items should usually be documented as future production improvements rather than built.

When suggesting extra technology, explain which priority level it belongs to and why.

## Scope Boundaries

Do not implement unless explicitly asked later:

- Full frontend
- User accounts
- Login/authentication
- Redis
- OpenSearch/Elasticsearch
- Kafka/SNS/SQS
- Kubernetes
- OpenTofu/Terraform
- Spring AI
- Complex copyright/work modelling

Mention these as future production improvements where relevant.