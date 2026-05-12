# Presentation Notes: Document Generator

These notes support the LDMS Senior Java Engineer final interview discussion. They are not a script; they are prompts for a clear whiteboard walkthrough.

## One-minute summary

I built a small Spring Boot backend that treats document generation as a durable business process, not just a file-rendering utility.

The backend owns templates, immutable template versions, generation requests, generated document metadata, and audit events. The frontend can initiate and inspect the process, but the backend remains the source of truth for status, version selection, metadata, and audit history.

## Core flow

1. An operations or lending user selects a document template in the frontend.
2. The frontend submits a generation request with a template/version and business payload.
3. The backend validates the request and records it as `RECEIVED` or `VALIDATED`.
4. The generation service creates deterministic generated-document metadata through a rendering boundary that could later be replaced by a real PDF/DOCX renderer.
5. The backend records generated document metadata, such as checksum and storage reference.
6. The request moves to `COMPLETED` or `FAILED`.
7. Each important lifecycle step creates an audit event.

## Whiteboard model

Suggested five core boxes:

- `document_template`
- `document_template_version`
- `document_generation_request`
- `generated_document`
- `audit_event`

Suggested relationships:

- One template has many immutable versions.
- One generation request uses exactly one template version.
- One generation request can produce one generated document in the first version.
- Audit events can reference a template, template version, request, or generated document.

## API shape to discuss

Example endpoints for the first implementation slice:

- `GET /api/v1/templates`
- `GET /api/v1/templates/{templateId}`
- `GET /api/v1/templates/{templateId}/versions`
- `POST /api/v1/generation-requests`
- `GET /api/v1/generation-requests/{requestId}`
- `GET /api/v1/generation-requests/{requestId}/audit-events`
- `GET /api/v1/generated-documents/{documentId}`

Template administration endpoints are deliberately deferred; seeded templates are enough for the first credible generation workflow.

## Status lifecycle

A small lifecycle is enough for the demo:

- `RECEIVED`
- `VALIDATED`
- `GENERATING`
- `COMPLETED`
- `FAILED`

For the first implementation, this can be synchronous internally. The explicit statuses still make the design ready for asynchronous processing later.

## Auditability points

Emphasise:

- Exact template version used.
- Request timestamps.
- Actor/requester context, even before real authentication is added.
- Status changes with reasons.
- Generated document checksum or equivalent integrity metadata.
- Append-only audit events rather than mutable notes.

## Frontend flow without building a frontend

The frontend is intentionally not implemented. I would still document the flow because it shapes the API:

- Template selection screen.
- Form generated from template requirements or application context.
- Submit generation request.
- Status view.
- Completion view with generated document metadata.
- Audit view for operational users.

The UI should not invent status. It should read backend state.

## Why keep it small

This is an interview demo. The strongest version is one that is easy to run, test, and explain.

I would avoid adding Kafka, Redis, Kubernetes, object storage, authentication, or a real rendering engine until the core lifecycle is implemented and tested. Those can be discussed as production extensions.

## Future slices

- Slice 1: Implemented — seeded templates, generation requests, generated metadata, and audit events.
- Slice 2: Template administration — create templates, create template versions, draft/active/retired states, and approval workflow.
- Slice 3: Payload schemas — template-specific input schema, payload validation, and better client form metadata.
- Slice 4: Real rendering and storage — PDF/DOCX renderer, object storage, download authorisation, and retention policy.
- Slice 5: Async workflow — queue/worker, retries, cancel/regenerate, and notifications.

## Production extensions to mention if asked

- Asynchronous generation with a queue or job worker.
- Object storage for generated files.
- Virus scanning or content safety checks for uploaded templates.
- Authentication and role-based access control.
- Template approval workflow.
- Document retention policies.
- Encryption and key management.
- Observability dashboards and alerts.
- Idempotency keys for request submission.
- Webhook or callback notifications.

## Honest current-state note

The first backend slice is implemented and buildable: it uses seeded template versions, synchronous demo generation, deterministic metadata, and audit events. Real rendering, storage, authentication, queues, and frontend work remain future production extensions.
