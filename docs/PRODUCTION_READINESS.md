# Production Readiness Plan

This document separates what is essential for the take-home submission from what would be important in a full production system.

The goal is to show production awareness while keeping the implementation realistic for a time-boxed exercise.

## P0 — Must-have for the take-home

These are required for a credible submission.

### Core functionality

- Create artists
- Edit artist primary name
- Add aliases to artists
- Add tracks to artists
- Fetch tracks for an artist
- Fetch Artist of the Day

### Domain correctness

- Stable artist identity using UUIDs
- Artist aliases modelled explicitly
- Tracks linked to stable artist IDs, not names
- Aliases excluded from Artist of the Day rotation
- Deterministic fair Artist of the Day cycle

### API quality

- RESTful endpoints under `/api/v1`
- Request and response DTOs
- Jakarta Validation on incoming requests
- Clean error responses using Problem Details-style responses
- Pagination for track retrieval
- Lightweight HATEOAS links for discoverability
- OpenAPI / Swagger documentation

### Persistence

- PostgreSQL as source of truth
- Flyway migrations
- Useful constraints and indexes
- No reliance on Hibernate auto-DDL for schema creation

### Testing

- Unit tests for Artist of the Day rotation
- API/integration tests for main user flows
- Testcontainers-backed PostgreSQL tests where useful

### Local run

- Clear README instructions
- Docker Compose for PostgreSQL
- Application can run locally from Gradle
- Final Docker Compose support for app + database if time allows

## P1 — Important if time allows

These are valuable production-readiness improvements, but should not block the core task.

### Observability

- Spring Boot Actuator health endpoint
- Prometheus metrics endpoint
- Basic structured logging
- Useful application logs around create/update operations
- Document key SLIs:
    - request latency
    - error rate
    - database connection health
    - Artist of the Day endpoint availability

### CI/CD

- GitHub Actions workflow
- Build and test on pull request
- Run unit and integration tests
- Cache Gradle dependencies
- Optional dependency/security scan
- Optional Docker image build

### Containerisation

- Dockerfile
- Docker Compose running app + PostgreSQL
- Environment-variable based configuration

### Documentation

- Architecture document
- Decision log
- Production readiness plan
- API examples
- Known trade-offs and future improvements

## P2 — Future production improvements

These are realistic production concerns, but outside the time-boxed take-home implementation.

### Security

- OAuth2/OIDC resource server
- Protect write operations with scopes/roles
- Secrets managed through AWS Secrets Manager or equivalent
- Rate limiting at gateway/load balancer level

### Scalability

- Daily cache or precomputed `artist_of_the_day` table for Artist of the Day
- Read replicas for high-volume reads
- Cursor/keyset pagination for very large catalogues
- OpenSearch for fuzzy artist, alias, and track search

### Architecture evolution

- Publish metadata events when artists, aliases, or tracks change
- Use SNS/SQS or Kafka for async consumers
- Split into separate services only if ownership, scale, or deployment cadence require it

### Deployment

- AWS ECS/Fargate
- RDS PostgreSQL
- Application Load Balancer
- OpenTofu infrastructure definitions
- Blue/green or rolling deployments

### Observability maturity

- Distributed tracing with OpenTelemetry
- Dashboards and alerting
- SLOs for key endpoints
- Error budgets
- Runbook for incidents

## Deliberate non-goals for this submission

The following are intentionally not implemented unless time allows:

- Full frontend
- User login system
- Redis
- OpenSearch
- Messaging/event bus
- Kubernetes
- OpenTofu
- Full tracing stack
- Full copyright/rightsholder/work modelling