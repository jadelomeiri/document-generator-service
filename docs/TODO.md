# TODO

The first backend slice is complete for the LDMS interview demo. The service can list seeded templates and template versions, create a deterministic simulated generation request, persist generated document metadata, and expose audit events for the request lifecycle.

## Completed for interview demo

- [x] Documentation foundation for the document-generator direction, task brief, decision log, and presentation notes.
- [x] Repository alignment around the document-generator package, application name, configuration, README, and docs.
- [x] Docker Compose PostgreSQL, Flyway, Spring Data JPA, Spring Web, Actuator, SpringDoc OpenAPI, Testcontainers, Checkstyle, and GitHub Actions support retained.
- [x] Domain model and Flyway schema for document templates, immutable template versions, generation requests, generated document metadata, and append-only audit events.
- [x] Seeded interview demo templates and active template versions.
- [x] API DTOs and controllers for listing templates, listing template versions, creating generation requests, reading generation requests, reading generated document metadata, and reading request audit events.
- [x] Jakarta Validation on request DTOs and Problem Details-style error responses.
- [x] Generation request creation with explicit lifecycle transitions through `RECEIVED`, `VALIDATED`, `GENERATING`, and `COMPLETED`.
- [x] Simulated deterministic generation metadata, including content type, checksum, storage reference, and template-version traceability.
- [x] Audit event creation for request creation, generation completion, and generation failure cases.
- [x] Integration coverage for seeded template APIs, request creation, generated document metadata, validation failures, not-found responses, audit events, and failure handling.
- [x] README run instructions and API examples updated for the implemented backend slice.

## Future / conditional follow-up

These are intentionally conditional. They should be added only if the interview discussion or a later product requirement expands the backend scope.

- [ ] Deeper payload-shape validation for known template inputs.
- [ ] Invalid status-transition tests if more lifecycle mutation paths are introduced.
- [ ] Template creation.
- [ ] Template version creation and activation rules.
- [ ] Unit tests for richer status transition rules.
- [ ] Idempotency keys for safe request retries.
- [ ] Pagination for broader request/audit listing endpoints.

## Deliberately out of scope

These are not needed for the focused backend interview demo and should remain documented as future production improvements unless explicitly requested.

- [ ] Frontend application.
- [ ] Authentication or authorization.
- [ ] Real PDF/DOCX rendering.
- [ ] Object storage integration.
- [ ] Background queues or workers.
- [ ] Redis.
- [ ] Kafka or other event streaming platforms.
- [ ] Kubernetes.
- [ ] Workflow engine.
- [ ] Large-scale infrastructure automation.
- [ ] Template admin workflow beyond the seeded demo data.
