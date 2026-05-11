# Document Generator Service

A small, production-minded Spring Boot backend demo for an LDMS Senior Java Engineer final interview whiteboard exercise: **Design a Document Generator**.

This repository was copied from a previous Spring Boot technical task and is being refocused into a fintech/lending document generation service. The first step is this documentation foundation. The existing Java application has not been replaced yet, so the codebase may still contain legacy music-metadata implementation details until the backend is intentionally migrated.

## Goal

Model the backend of a document generator used in a lending or fintech context. The service should show how a backend can safely manage document templates, accept document generation requests, track generated document metadata, and retain an audit trail for operational and compliance review.

The aim is not to build a full enterprise document platform. The target is a focused, interview-friendly service that demonstrates senior engineering judgement: clear boundaries, auditability, versioning, request status tracking, testability, and API design.

## Intended domain model

The planned backend will focus on four concepts:

| Concept | Purpose |
| --- | --- |
| Document template | A named, versioned template such as loan agreement, disclosure pack, statement, or offer letter. |
| Generation request | A request to create a document from a specific template version and input payload. |
| Generated document metadata | Metadata about the produced document, such as document id, status, checksum, storage reference, timestamps, and requester context. |
| Audit event | Append-only events recording important lifecycle actions for templates, requests, and generated documents. |

The backend is the source of truth for request state, generated document metadata, template versions, and audit history.

## What is implemented today

Implemented today:

- Documentation foundation for the new Document Generator direction.
- Existing Spring Boot project structure, build tooling, Docker support, database migration setup, tests, and CI inherited from the previous task.

Not implemented yet:

- Document template APIs.
- Template version persistence.
- Document generation request APIs.
- Request status workflow.
- Generated document metadata persistence.
- Audit event persistence and APIs.
- Document rendering or file storage integration.

The existing Java code is deliberately left untouched at this stage. It will be migrated in later steps after the design direction is documented.

## Planned backend capabilities

The first production-minded slice should include:

- Manage document templates and immutable template versions.
- Create document generation requests against an explicit template version.
- Track request status, for example `RECEIVED`, `VALIDATED`, `GENERATING`, `COMPLETED`, and `FAILED`.
- Store generated document metadata without exposing implementation details of the storage provider.
- Record audit events for meaningful lifecycle changes.
- Provide clear REST APIs with validation and Problem Details-style errors.
- Use PostgreSQL as the source of truth and Flyway for schema migrations.
- Add tests around validation, state transitions, audit creation, and API behaviour.

## Frontend scope

A frontend will not be implemented for this interview demo.

The expected frontend flow will be documented instead:

1. An operations or lending user selects a document template.
2. The UI shows the active template version and required input fields.
3. The user submits a generation request.
4. The UI polls or refreshes request status.
5. When completed, the UI shows generated document metadata and a download/open action if supported by the backend.
6. Audit history is available to authorised operational users.

The backend remains the source of truth for statuses, template versions, generated metadata, and audit events. The UI should not infer lifecycle state independently.

## Deliberately out of scope

To keep the exercise small and focused, these are intentionally out of scope unless explicitly requested later:

- Full frontend implementation.
- User accounts, login, or authentication flows.
- Complex role-based access control.
- Real PDF/DOCX rendering engine integration.
- External object storage integration such as S3.
- Kafka, SQS, SNS, or event streaming.
- Redis or distributed caching.
- Elasticsearch/OpenSearch.
- Kubernetes or infrastructure-as-code.
- Full workflow orchestration.
- Complex credit, underwriting, or legal-document modelling.

These can be discussed as production extensions, but they should not distract from the core backend design.

## Technology direction

The intended stack remains a pragmatic JVM backend stack:

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Jakarta Validation
- PostgreSQL
- Flyway
- Spring Boot Actuator
- SpringDoc OpenAPI
- JUnit 5
- Testcontainers
- Docker Compose for local support

Version details will be confirmed during implementation rather than over-specified in documentation before the code migration.

## Local development

The inherited project currently uses Gradle:

```bash
./gradlew clean build
```

Local runtime commands will be updated once the Java implementation is migrated from the previous task to the document generator domain.

## Documentation map

- `docs/TASK_BRIEF.md` - concise interview exercise brief and scope.
- `docs/DECISIONS.md` - design decisions, trade-offs, and alternatives considered.
- `docs/PRESENTATION_NOTES.md` - whiteboard / final interview talking points.
- `docs/TODO.md` - implementation plan and sequencing.

## Current status

This repository is in the **documentation foundation** phase for the Document Generator exercise. Java code changes are intentionally deferred.
