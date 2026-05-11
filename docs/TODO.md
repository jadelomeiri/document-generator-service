# TODO

This TODO tracks the remaining Document Generator Service work. Keep the scope small, production-minded, and interview-friendly.

## Phase 0: Documentation foundation

- [x] Create README for the Document Generator direction.
- [x] Create task brief for the LDMS interview exercise.
- [x] Create decision log with trade-offs and alternatives.
- [x] Create presentation notes for the whiteboard discussion.
- [x] Create implementation TODO list.
- [x] Completed the documentation foundation before implementation work.

## Phase 1: Repository alignment

- [x] Review package names, application names, and configuration for document-generator wording.
- [x] Preserve useful infrastructure such as Gradle, Docker Compose, Flyway, Testcontainers, OpenAPI, and CI.
- [x] Keep non-code documentation aligned with the document-generator domain.
- [x] Confirm local build and static checks after code changes.

## Phase 2: Domain and persistence design

- [x] Define entities for document templates.
- [x] Define immutable template versions.
- [x] Define generation requests with explicit statuses.
- [x] Define generated document metadata.
- [x] Define append-only audit events.
- [x] Add Flyway migrations for the document generator schema.
- [x] Keep Flyway migrations aligned with the document-generator schema.

## Phase 3: API implementation

- [x] Add request and response DTOs for templates.
- [x] Add request and response DTOs for template versions.
- [x] Add request and response DTOs for generation requests.
- [x] Add request and response DTOs for generated document metadata.
- [x] Add validation for required fields.
- [ ] Add deeper payload-shape validation and explicit invalid status-transition tests if/when mutation endpoints are added.
- [x] Add Problem Details-style error responses.
- [x] Preserve SpringDoc OpenAPI for the new API.

## Phase 4: Business behaviour

- [ ] Implement template creation if template administration becomes in scope. Current first slice lists seeded templates only.
- [ ] Implement template version creation and activation rules if template administration becomes in scope.
- [x] Implement generation request creation.
- [x] Implement request status transitions.
- [x] Implement a simple generation boundary that produces metadata without a real renderer.
- [x] Implement audit event creation for lifecycle changes.
- [x] Keep document bytes and external storage out of scope for the first slice.

## Phase 5: Testing

- [ ] Add unit tests for status transition rules if more lifecycle branches are introduced.
- [x] Add integration coverage for audit event creation.
- [x] Add API tests for validation failures and happy paths.
- [x] Add repository/integration tests using PostgreSQL/Testcontainers where useful.
- [x] Run the full Gradle build or record the environment limitation before committing Java changes.

## Phase 6: Production-minded polish

- [x] Retain Spring Boot Actuator support.
- [x] Confirm Docker Compose supports local PostgreSQL development.
- [x] Confirm API examples are documented.
- [ ] Update presentation notes with implemented endpoints and trade-offs.
- [x] Update README with accurate run instructions.

## Deliberately out of scope for now

- [ ] Full frontend.
- [ ] Authentication and login.
- [ ] Real PDF/DOCX rendering.
- [ ] Object storage integration.
- [ ] Kafka/SQS/SNS or other messaging.
- [ ] Redis.
- [ ] Elasticsearch/OpenSearch.
- [ ] Kubernetes.
- [ ] Infrastructure-as-code.
- [ ] Full workflow engine.
