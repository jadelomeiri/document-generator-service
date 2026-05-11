# TODO

This TODO tracks the migration from the previous Spring Boot task into the Document Generator Service. Keep the scope small, production-minded, and interview-friendly.

## Phase 0: Documentation foundation

- [x] Create README for the Document Generator direction.
- [x] Create task brief for the LDMS interview exercise.
- [x] Create decision log with trade-offs and alternatives.
- [x] Create presentation notes for the whiteboard discussion.
- [x] Create implementation TODO list.
- [x] Leave Java code unchanged during documentation foundation.

## Phase 1: Repository alignment before Java migration

- [ ] Review existing package names, application names, and configuration for legacy music-metadata wording.
- [ ] Decide whether to preserve useful infrastructure from the previous task, such as Gradle, Docker Compose, Flyway, Testcontainers, OpenAPI, and CI.
- [ ] Update non-code documentation that still refers to the old domain where it would confuse the new exercise.
- [ ] Confirm local build still works before domain code changes.

## Phase 2: Domain and persistence design

- [ ] Define entities for document templates.
- [ ] Define immutable template versions.
- [ ] Define generation requests with explicit statuses.
- [ ] Define generated document metadata.
- [ ] Define append-only audit events.
- [ ] Add Flyway migrations for the document generator schema.
- [ ] Remove or replace legacy music-domain migrations only when the Java migration starts.

## Phase 3: API implementation

- [ ] Add request and response DTOs for templates.
- [ ] Add request and response DTOs for template versions.
- [ ] Add request and response DTOs for generation requests.
- [ ] Add request and response DTOs for generated document metadata.
- [ ] Add validation for required fields, payload shape, and status transitions.
- [ ] Add Problem Details-style error responses.
- [ ] Add OpenAPI documentation for the new API.

## Phase 4: Business behaviour

- [ ] Implement template creation.
- [ ] Implement template version creation and activation rules.
- [ ] Implement generation request creation.
- [ ] Implement request status transitions.
- [ ] Implement a simple generation boundary that produces metadata without a real renderer.
- [ ] Implement audit event creation for lifecycle changes.
- [ ] Keep document bytes and external storage out of scope for the first slice.

## Phase 5: Testing

- [ ] Add unit tests for status transition rules.
- [ ] Add service tests for audit event creation.
- [ ] Add API tests for validation failures and happy paths.
- [ ] Add repository/integration tests using PostgreSQL/Testcontainers where useful.
- [ ] Run the full Gradle build before committing Java changes.

## Phase 6: Production-minded polish

- [ ] Add actuator health checks if not already retained from the previous task.
- [ ] Confirm Docker Compose supports local development.
- [ ] Confirm API examples are documented.
- [ ] Update presentation notes with implemented endpoints and trade-offs.
- [ ] Update README with accurate run instructions after the Java migration.

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
