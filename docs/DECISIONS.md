# Decisions

This document records the initial design direction for the Document Generator Service. It is intentionally pragmatic: enough structure to show production thinking, without turning an interview exercise into a platform rewrite.

## 1. Build a backend demo, not a full document platform

**Decision:** Keep the project as a small Spring Boot backend demo focused on the core document generation lifecycle.

**Why:** The interview exercise is about design judgement. A focused backend can show domain modelling, API design, persistence, validation, auditability, and testing without spending time on frontend, infrastructure, or vendor integrations.

**Alternatives considered:**

- Building a full frontend and backend together. Rejected because it dilutes the backend design discussion.
- Designing a full enterprise document-generation platform. Rejected because it would be too broad for a small interview demo.

## 2. Use the backend as the source of truth

**Decision:** Store template versions, generation requests, generated document metadata, statuses, and audit events in the backend database.

**Why:** In lending and fintech workflows, traceability matters. The system should be able to explain which template version was used, when a request was made, what happened to it, and what document metadata resulted.

**Alternatives considered:**

- Letting the frontend infer status from rendering calls. Rejected because it is unreliable and weak for audit.
- Treating generated files as the only source of truth. Rejected because file storage alone does not capture lifecycle, validation, or audit events cleanly.

## 3. Model template versions explicitly

**Decision:** A generation request should reference a specific immutable template version, not only a template name.

**Why:** Lending documents can change over time. A generated document must be traceable to the exact template version used at generation time.

**Alternatives considered:**

- Updating templates in place. Rejected because it breaks historical traceability.
- Storing only a free-text template name on each request. Rejected because it is too weak for audit and repeatability.

## 4. Manage request status explicitly

**Decision:** Generation requests should have a clear status lifecycle, such as `RECEIVED`, `VALIDATED`, `GENERATING`, `COMPLETED`, and `FAILED`.

**Why:** Status management gives clients a simple contract and gives the backend a reliable way to represent progress, failure, and retry discussions.

**Alternatives considered:**

- Return a generated document synchronously and store no request state. Rejected because it hides operational behaviour and does not scale well to slower rendering.
- Use a full workflow engine. Rejected for the first version because it is too heavy for the exercise.

## 5. Store generated document metadata separately from document bytes

**Decision:** The first version should store metadata such as generated document id, request id, template version id, checksum, storage reference, status, timestamps, and failure reason. Actual document bytes can be represented by a storage reference and left for a later integration.

**Why:** Metadata is central to the backend design and audit story. Real rendering and storage integrations are production concerns, but not necessary to demonstrate the core model.

**Alternatives considered:**

- Store binary documents directly in PostgreSQL. Rejected as unnecessary for the interview demo and often not the desired production storage approach.
- Integrate object storage immediately. Rejected because it adds infrastructure before the domain model is stable.

## 6. Use append-only audit events for important lifecycle actions

**Decision:** Record audit events for meaningful actions, such as template creation, template version activation, generation request creation, status changes, generation completion, and generation failure.

**Why:** Auditability is a first-class requirement in fintech/lending systems. An append-only audit log is simple to explain and useful for troubleshooting and compliance-style questions.

**Alternatives considered:**

- Rely only on application logs. Rejected because logs are not a durable domain-level audit record.
- Full event sourcing. Rejected because it is more complexity than needed for this exercise.

## 7. Keep APIs clear and boring

**Decision:** Use simple REST APIs with request/response DTOs, validation, pagination where needed, and Problem Details-style errors.

**Why:** Clear APIs are easier to test, document, and discuss. The goal is not to demonstrate clever API patterns; it is to demonstrate reliable backend design.

**Alternatives considered:**

- GraphQL. Rejected because the use case does not require client-driven graph traversal.
- Hypermedia-heavy API design. Rejected because it can distract from the domain model.

## 8. Keep rendering abstract in the first slice

**Decision:** Do not implement a real PDF/DOCX renderer in the first backend slice. Use a small generation service boundary that can later be replaced by a real renderer.

**Why:** Rendering engines introduce many details: fonts, layouts, signatures, storage, document previews, and vendor behaviour. Those are valid production concerns but not needed to establish the backend lifecycle.

**Alternatives considered:**

- Integrate a rendering library immediately. Rejected because it would dominate the exercise.
- Mock everything and skip metadata. Rejected because the backend needs enough real behaviour to be meaningful.

## 9. Prefer synchronous implementation first, with asynchronous design seams

**Decision:** Start with a simple synchronous service path that still records request statuses. Leave room for asynchronous processing later.

**Why:** The first version stays small and testable, while the status model keeps the design compatible with queues or background workers later.

**Alternatives considered:**

- Add Kafka, SQS, or a job queue immediately. Rejected because it is unnecessary infrastructure for the first implementation.
- Avoid status management until async processing exists. Rejected because status is valuable even in the synchronous first slice.

## 10. Use PostgreSQL and Flyway for persistence

**Decision:** Use PostgreSQL as the source of truth and Flyway migrations for schema changes.

**Why:** This gives durable state, explicit schema evolution, and a production-like local development story.

**Alternatives considered:**

- Hibernate auto-DDL. Rejected because migrations are clearer and safer for production-minded work.
- In-memory persistence. Rejected because auditability and state transitions should be tested against realistic persistence.

## 11. Test domain rules and API behaviour

**Decision:** Add tests around validation, request status transitions, audit event creation, template version selection, and API errors.

**Why:** These are the behaviours most likely to matter in an interview discussion and in a production service.

**Alternatives considered:**

- Only controller smoke tests. Rejected because they do not prove core lifecycle behaviour.
- Heavy end-to-end tests for every path. Rejected because the project should remain small.

## 12. Do not implement authentication in the first slice

**Decision:** Authentication and user management are out of scope for the first version.

**Why:** Auth is important in a real fintech system, but implementing it would consume time without improving the core document generator model. The API can still include requester context fields to show where authenticated identity would later be attached.

**Alternatives considered:**

- Build login and roles. Rejected as too broad for this exercise.
- Ignore actor context entirely. Rejected because audit events should still make room for actor information.

## 13. Document the frontend flow but do not build it

**Decision:** Explain how a frontend would use the backend, but do not implement frontend code.

**Why:** The exercise is backend-focused. A documented flow demonstrates product thinking without leaving the Java implementation underdeveloped.

**Alternatives considered:**

- Build a simple UI. Rejected because it adds surface area and testing cost.
- Ignore frontend needs. Rejected because API design should still support realistic user flows.

## 14. Migrate the backend in one focused first slice

**Decision:** Replace the previous domain with a small document-generator slice covering templates, template versions, generation requests, generated document metadata, and audit events.

**Why:** The documentation direction is now clear, so keeping the old Java domain would be more confusing than useful. A focused migration keeps the implementation reviewable while making the service runnable and credible.

**Alternatives considered:**

- Keep only documentation and defer Java changes. Rejected because the interview demo now needs a buildable backend slice.
- Build template administration, auth, rendering, storage, and queues immediately. Rejected because that would over-expand the exercise beyond the core lifecycle.
