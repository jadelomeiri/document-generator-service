# Architecture

## Current state

The repository now contains the first backend slice of the Document Generator Service. It provides PostgreSQL/Flyway persistence, seeded templates, synchronous demo generation, generated document metadata, audit events, REST APIs, Problem Details-style errors, and Testcontainers-backed integration tests.

The goal remains a small, production-minded Spring Boot backend for a fintech/lending document generator. It should be straightforward to run, test, review, and explain in an interview.

## Architectural style

Use a modular monolith with clear package boundaries rather than starting with microservices.

Implemented package areas:

- `template` for template and template-version persistence/API.
- `generation` for request lifecycle orchestration/API.
- `document` for generated document metadata/API.
- `audit` for audit event persistence and request audit history.
- `common` for shared API and error handling support.

This keeps the first implementation simple while still leaving clean seams for future extraction if the system grows.

## High-level flow

1. A client submits a document generation request against a specific template version.
2. The backend validates the request and stores a generation request record.
3. The backend records an audit event for request creation.
4. A generation service boundary processes the request.
5. The backend stores generated document metadata, such as checksum, content type, storage reference, and completion timestamp.
6. The backend updates request status to a terminal state and records another audit event.

For the first implementation, this flow can run synchronously. The explicit request status model keeps the design compatible with later asynchronous processing.

## Domain model

The domain model has five first-class concepts. They are listed separately because template versioning, request lifecycle, generated metadata, and audit history should not be collapsed into one generic document record.

### Document template

A document template represents a business document family, such as a loan agreement, disclosure pack, statement, or offer letter.

Templates should have stable UUID identity and human-readable names. The template record should not be mutated in a way that breaks historical traceability.

### Template version

A template version is an immutable snapshot used for generation.

Generation requests should reference a specific template version so the system can answer, later, exactly which template was used. A later version of the same template should not change the meaning of older generated documents.

### Generation request

A generation request is the backend-owned record of a request to produce a document.

It should include the selected template version, requester context, submitted payload or payload reference, timestamps, current status, and failure reason when relevant.

A deliberately small first status lifecycle is enough:

- `RECEIVED`
- `VALIDATED`
- `GENERATING`
- `COMPLETED`
- `FAILED`

### Generated document metadata

Generated document metadata describes the output without requiring the first version to store the actual document bytes.

Implemented metadata includes generated document id, generation request id, template version id, content type, checksum, storage reference, and creation timestamp. Generation failures are stored on the generation request rather than on a generated document row, because no document is created for a failed request.

### Audit event

Audit events are append-only records for meaningful lifecycle actions.

Examples include template creation, template version creation, template version activation, generation request creation, status changes, generation completion, and generation failure.

Audit events should include enough context to support operational review: event type, target resource, timestamp, actor/requester context, and structured event details where useful.

## API boundaries

The API should expose request/response DTOs rather than persistence entities.

Implemented first-slice resources:

- Templates.
- Template versions.
- Generation requests.
- Generated document metadata.
- Audit events for a generation request.

The API should use validation at the boundary and Problem Details-style error responses for invalid input and missing resources. Invalid state transitions and conflicts are reserved for future mutation endpoints.

## Persistence

PostgreSQL should be the source of truth. Flyway should own schema changes.

Important persistence expectations:

- UUID primary identifiers for stable external identity.
- Immutable template versions after creation.
- Indexed lookups for request id, template id, template version id, and audit target references.
- Explicit status columns rather than deriving status from logs.
- Append-only audit table.

## Rendering boundary

The first implementation does not integrate a real PDF or DOCX rendering engine.

Instead, it uses a small generation service boundary that produces deterministic metadata for the demo and can later be replaced by a real renderer. This keeps the first slice focused on lifecycle, persistence, auditability, and API design.

## Frontend relationship

The frontend is not implemented. It is a consumer of backend state.

The backend should be the source of truth for:

- Template and template version selection.
- Generation request status.
- Generated document metadata.
- Audit history.

The frontend should not infer request state independently.

## Deliberately deferred production concerns

These are valid production topics, but they are not part of the first implementation slice:

- Authentication and fine-grained authorisation.
- Real rendering engine integration.
- Object storage integration.
- Queue-backed asynchronous generation.
- Document retention and legal hold policies.
- Malware scanning for uploaded template assets.
- Workflow approvals for template publication.
- Search infrastructure.
- Distributed caching.
- Kubernetes or infrastructure-as-code.
