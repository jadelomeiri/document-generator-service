# AGENTS.md

## Project

This is a small, production-minded Spring Boot Document Generator Service for an LDMS Senior Java Engineer final interview.

The implementation should be easy to run, test, review, and explain in an interview. Keep the service intentionally scoped: credible enough to show senior engineering judgement, but not expanded into a large platform.

The backend is the source of truth. API contracts, persistence, audit history, and template-version traceability should be represented in the service rather than delegated to a frontend or external workflow system.

## Stack to Preserve

- Java 25
- Spring Boot 4
- PostgreSQL
- Flyway
- Spring Web
- Spring Data JPA
- Jakarta Validation
- Spring Boot Actuator
- SpringDoc OpenAPI
- JUnit 5
- Testcontainers
- Checkstyle
- Docker and Docker Compose for local/runtime support
- GitHub Actions CI
- Project documentation under `README.md` and `docs/`

## Engineering Standards

- Keep the app small, production-minded, and interview-friendly.
- Prefer simple, readable code over clever abstractions.
- Prioritise build correctness and a clean developer experience.
- Do not expose JPA entities directly from controllers.
- Use request/response DTOs or records for API contracts.
- Validate request DTOs with Jakarta Validation.
- Use clean error handling with Problem Details-style responses.
- Use Flyway migrations, not Hibernate auto-DDL.
- Add clear tests for business rules, API behaviour, persistence, and audit expectations.
- Keep Checkstyle passing and avoid style-only churn.
- Keep README and docs updated as implementation decisions change.
- When documenting decisions, include alternatives considered and why they were not chosen.
- Avoid generic enterprise phrasing. Prefer clear, pragmatic reasoning.

## Domain Model

Keep the model focused on the document generation lifecycle:

- `DocumentTemplate`: a named template family, such as a loan agreement or customer statement.
- `DocumentTemplateVersion`: an immutable version of a template used by generation requests so historical outputs remain traceable.
- `DocumentGenerationRequest`: a durable request to generate a document from a specific template version and input payload.
- `GeneratedDocument`: metadata about the generated output, including content type, checksum, storage reference, status, timestamps, and template version linkage.
- `AuditEvent`: append-only lifecycle events that make important request and document actions explainable.

## Domain Rules and Priorities

- Treat template versions as immutable once used by a generation request.
- Preserve traceability from every generated document back to the exact template version used.
- Record meaningful audit events for lifecycle transitions and important failure cases.
- Keep generation deterministic and testable where practical.
- Use bounded and paginated retrieval for collections that can grow.
- Prefer honest metadata and simulated output over pretending to provide full production rendering.
- Make failure states explicit and observable rather than hiding them behind generic success responses.
- Prioritise documentation honesty: explain what is implemented, what is simulated, and what would be added in production.

## Prioritisation

Use `docs/PRODUCTION_READINESS.md` to prioritise work.

Complete P0 before implementing P1.

Do not implement P2 items unless explicitly asked. P2 items should usually be documented as future production improvements rather than built.

When suggesting extra technology, explain which priority level it belongs to and why.

## Scope Boundaries

Do not implement unless explicitly asked later:

- Frontend application
- Authentication or authorization
- Real document rendering engine
- Object storage integration
- Background queues or workers
- Redis
- Kafka or other event streaming platforms
- Kubernetes
- Workflow engine
- Large-scale infrastructure automation

Mention these as future production improvements where relevant, but keep the current codebase focused on a small backend interview demo.
