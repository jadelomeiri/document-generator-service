# Production Readiness

This document prioritises production-minded work for the Document Generator Service. The first backend slice has now been implemented; this file separates what is complete from useful follow-up work.

The goal is to keep the system small enough for an interview demo while showing the right instincts for fintech/lending software: traceability, versioning, clear state, validation, and testability.

## Priority levels

- **P0**: Required for a credible first backend slice.
- **P1**: Useful production-minded polish once P0 is complete.
- **P2**: Future production improvements to discuss, not build unless explicitly requested.

## P0: First credible backend slice (implemented)

### Domain and persistence

P0 starts with the same five first-class concepts used across the design docs:

- Document templates with stable UUID identity.
- Immutable template versions.
- Generation requests linked to a specific template version.
- Explicit request status lifecycle.
- Generated document metadata separate from document bytes.
- Append-only audit events for meaningful lifecycle actions.
- PostgreSQL as the source of truth.
- Flyway migrations for schema changes.

### API behaviour

- Request/response DTOs rather than exposing persistence entities.
- Jakarta Validation for API inputs.
- Problem Details-style errors for validation failures and missing resources. Invalid state transitions and conflicts belong with future mutation endpoints.
- Clear endpoint names for templates, template versions, generation requests, generated document metadata, and audit events.
- Bounded retrieval for collections that can grow. The first slice only exposes small seeded template lists and per-request audit history; broader request listing and audit pagination remain out of scope.

### Business rules

- A generation request must reference an existing template version.
- A completed request should have generated document metadata.
- A failed request should capture a reason suitable for operations without leaking sensitive internals.
- Template versions should not be mutated after use by a generation request.
- Status transitions should be explicit and tested.
- Audit events should be recorded for request creation, status changes, completion, and failure.

### Testing

- Focused tests for the generation workflow, generated document metadata, audit event creation, and missing template-version errors.
- Testcontainers-backed PostgreSQL integration tests for the API and persistence path.
- The full Gradle build passes locally.

### Documentation

- README reflects the current implementation state honestly.
- Task brief, decisions, architecture, diagrams, frontend flow, production readiness, presentation notes, and TODO stay aligned.
- API examples are added after endpoints exist.

## P1: Production-minded polish after P0

### Operational readiness

- Actuator health endpoints.
- Liveness and readiness probes if the runtime setup supports them.
- Structured logging with request correlation.
- Basic application metrics for request counts, failures, and generation duration.
- Docker Compose support for local application and database execution.

### API and client experience

- OpenAPI documentation for implemented endpoints.
- Clear examples for common request and response bodies.
- Consistent error response structure.
- Idempotency key support for generation request creation if duplicate submissions become a realistic concern.

### Data and audit quality

- Actor/requester context carried through request and audit records.
- Failure reason taxonomy for common operational failures.
- Indexes for common lookups by request status, template version, created timestamp, and audit target.
- Retention policy documented, even if not automated in the demo.

### Test quality

- Testcontainers-backed PostgreSQL tests for migration and persistence behaviour.
- Contract-style tests for API response shapes.
- Edge-case tests for invalid status transitions and duplicate active template versions.

## P2: Future production improvements, not first-slice build items

These are valid production topics but should remain future discussion points unless explicitly requested.

### Asynchronous processing

- Queue-backed generation workers.
- Retry policy with dead-letter handling.
- Worker-level concurrency control.
- Timeout handling for slow rendering.

### Document storage and rendering

- Real PDF/DOCX rendering engine integration.
- Object storage for generated files.
- Content checksums and download authorisation.
- Malware scanning for uploaded template assets.
- Preview generation.

### Governance and security

- Authentication and authorisation.
- Template approval workflow.
- Document retention and legal hold policies.
- Encryption at rest and key management.
- Redaction or minimisation of sensitive payload data.

### Scale and platform

- Search indexing for operational lookup.
- Distributed cache for specific hot reads.
- Webhook or callback notifications.
- Kubernetes deployment manifests.
- Infrastructure-as-code.
- External observability dashboards and alerting.

## Readiness checklist before presenting

- [x] Documentation states clearly what is implemented and what is planned.
- [x] Documentation uses the document-generator domain consistently.
- [x] The first backend slice is implemented and buildable.
- [x] First implementation stays focused on templates, versions, requests, generated metadata, and audit events.
- [x] P2 ideas are described as future improvements, not part of the first build.
