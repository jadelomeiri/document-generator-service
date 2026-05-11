# Document Generator Service

A small, production-minded Spring Boot backend demo for an LDMS Senior Java Engineer final interview whiteboard exercise: **Design a Document Generator**.

The implemented slice focuses on template discovery, synchronous demo generation, generated document metadata, and audit history for a fintech/lending document-generation backend.

## Goal

Model the backend of a document generator used in a lending or fintech context. The service shows how a backend can manage document templates, accept document generation requests, record generated document metadata, and retain an audit trail for operational and compliance review.

The aim is not to build a full enterprise document platform. The target is a focused, interview-friendly service that demonstrates clear boundaries, auditability, versioning, request status management, testability, and API design.

## Implemented domain model

The backend focuses on five first-class concepts:

| Concept | Purpose |
| --- | --- |
| `DocumentTemplate` | A named template family such as Loan Agreement or Customer Statement. |
| `DocumentTemplateVersion` | An immutable version used by generation requests so historical documents remain traceable. |
| `DocumentGenerationRequest` | A request to create a document from a specific template version and input payload. |
| `GeneratedDocument` | Metadata about the produced document, including content type, checksum, storage reference, timestamps, and template version. |
| `AuditEvent` | Events recording important lifecycle actions for generation requests. |

The backend is the source of truth for templates, template versions, request state, generated document metadata, and audit history.

## What is implemented today

Implemented:

- PostgreSQL/Flyway schema for templates, template versions, generation requests, generated documents, and audit events.
- Seed data for two active PDF templates:
  - Loan Agreement v1 PDF.
  - Customer Statement v1 PDF.
- REST endpoints for listing templates, reading versions, creating generation requests, reading requests, reading generated document metadata, and viewing request audit events.
- A synchronous generation workflow with statuses `RECEIVED`, `VALIDATED`, `GENERATING`, `COMPLETED`, and `FAILED`.
- Deterministic demo generated-document metadata using `demo://generated-documents/{requestId}` storage references and SHA-256 checksums.
- Problem Details-style errors for missing resources, invalid UUIDs, and validation failures.
- Focused integration tests using Testcontainers/PostgreSQL.

Not implemented:

- Real PDF/DOCX rendering.
- Object storage integration.
- Authentication or user accounts.
- Queues, Kafka/SQS/SNS, Redis, Elasticsearch/OpenSearch, Kubernetes, or workflow engines.
- Frontend UI.

## REST API

Base path: `/api/v1`

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/templates` | List seeded document templates. |
| `GET` | `/templates/{templateId}` | Read a template. |
| `GET` | `/templates/{templateId}/versions` | List versions for a template. |
| `POST` | `/generation-requests` | Create and synchronously process a demo generation request. |
| `GET` | `/generation-requests/{requestId}` | Read a generation request and generated document metadata when completed. |
| `GET` | `/generation-requests/{requestId}/audit-events` | List audit events for a generation request. |
| `GET` | `/generated-documents/{documentId}` | Read generated document metadata. |

Example request:

```bash
curl -X POST http://localhost:8080/api/v1/generation-requests \
  -H 'Content-Type: application/json' \
  -d '{
    "templateVersionId": "20000000-0000-0000-0000-000000000001",
    "customerReference": "customer-123",
    "requestedBy": "caseworker-456",
    "inputPayloadJson": "{\"loanAmount\":125000,\"currency\":\"GBP\"}"
  }'
```

## Frontend scope

A frontend is intentionally not implemented for this interview demo. A future UI would select a template, submit a generation request, poll or refresh status, display generated document metadata, and expose audit history to authorised operational users.

## Technology stack

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring HATEOAS
- Spring Data JPA
- Jakarta Validation
- PostgreSQL
- Flyway
- Spring Boot Actuator
- SpringDoc OpenAPI
- JUnit 5
- Testcontainers
- Docker Compose for local support

## Local development

Run the full quality gate:

```bash
./gradlew clean build
```

Start PostgreSQL for local runtime:

```bash
docker compose up -d postgres
```

Then run the application:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Swagger UI is available at `http://localhost:8080/swagger-ui.html` when the app is running. SpringDoc redirects that URL to the Swagger UI index page.

Useful smoke checks after startup:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/templates
curl http://localhost:8080/api/v1/templates/10000000-0000-0000-0000-000000000001/versions
curl -X POST http://localhost:8080/api/v1/generation-requests \
  -H 'Content-Type: application/json' \
  -d '{
    "templateVersionId": "20000000-0000-0000-0000-000000000001",
    "customerReference": "customer-123",
    "requestedBy": "caseworker-456",
    "inputPayloadJson": "{\"loanAmount\":125000,\"currency\":\"GBP\"}"
  }'
```

A successful POST returns a `COMPLETED` request with generated document metadata, a demo `demo://generated-documents/{requestId}` storage reference, and links for the generated document and request audit events.

The seeded template UUIDs are:

- Loan Agreement template: `10000000-0000-0000-0000-000000000001`
- Loan Agreement v1 PDF: `20000000-0000-0000-0000-000000000001`
- Customer Statement template: `10000000-0000-0000-0000-000000000002`
- Customer Statement v1 PDF: `20000000-0000-0000-0000-000000000002`

## Documentation map

- `docs/TASK_BRIEF.md` - concise interview exercise brief and scope.
- `docs/ARCHITECTURE.md` - backend architecture and request lifecycle.
- `docs/DIAGRAMS.md` - system context, components, data relationships, and request lifecycle diagrams.
- `docs/DECISIONS.md` - design decisions, trade-offs, and alternatives considered.
- `docs/FRONTEND_FLOW.md` - documented frontend flow without adding a frontend implementation.
- `docs/PRODUCTION_READINESS.md` - prioritised production-readiness backlog.
- `docs/TODO.md` - implementation checklist.
- `docs/PRESENTATION_NOTES.md` - interview talking points.
