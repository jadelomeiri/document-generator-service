# Music Metadata Service

A production-minded Java/Spring Boot take-home implementation of a music metadata API for a streaming-platform-like product. The service models canonical artists, realistic artist aliases, and artist tracks, with a deterministic Artist of the Day endpoint suitable for a homepage experience.

The implementation is intentionally scoped: it is more than toy CRUD, but avoids unnecessary platform complexity such as authentication, messaging, search infrastructure, Kubernetes, or frontend work.

## 1. Project overview

The service exposes a versioned REST API for:

- managing artists with stable UUID identity and a primary display name
- modelling aliases as separate records linked to the canonical artist
- adding and retrieving tracks for an artist
- selecting a deterministic, fair, cyclical Artist of the Day using UTC date and canonical artists only

It uses PostgreSQL as the source of truth, Flyway for schema migrations, Spring validation for API contracts, RFC 9457-style Problem Details responses for common API errors, lightweight HATEOAS links for discoverability, and OpenAPI/Swagger for interactive API exploration.

## 2. Implemented features

- Artist lifecycle:
  - create an artist
  - fetch an artist by UUID
  - update an artist primary name
- Artist alias management:
  - add an alias to an artist
  - list aliases for an artist
  - enforce aliases as metadata on the canonical artist rather than separate artists
- Track catalogue management:
  - add tracks to an artist
  - list tracks with bounded pagination (`page`, `size`, max size `100`)
  - normalise optional metadata such as blank genre and ISRC casing
  - reject duplicate ISRC values with `409 Conflict`
- Homepage support:
  - deterministic Artist of the Day
  - aliases excluded from the rotation so artists with many aliases are not overrepresented
- API quality:
  - request DTO validation with Jakarta Validation
  - Problem Details-style error responses for validation, missing artists, duplicates, and invalid UUIDs
  - lightweight `_links` responses using Spring HATEOAS
  - OpenAPI / Swagger UI
- Runtime and delivery:
  - PostgreSQL schema managed by Flyway
  - Dockerfile and Docker Compose support
  - environment-specific Spring profiles (`local`, `prod`)
  - Actuator health, liveness/readiness probes, and Prometheus metrics
  - JUnit 5 and Testcontainers-backed integration tests
  - Checkstyle wired into the Gradle build
  - GitHub Actions CI running the full build
  - Dependabot for Gradle and GitHub Actions updates

## 3. Tech stack

- Java 25
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring HATEOAS
- Jakarta Validation
- PostgreSQL
- Flyway
- Spring Boot Actuator
- Micrometer Prometheus registry
- SpringDoc OpenAPI / Swagger UI
- JUnit 5
- Testcontainers
- Gradle
- Checkstyle
- Docker / Docker Compose
- GitHub Actions CI
- Dependabot

## 4. API overview with key endpoints

Base path: `/api/v1`

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/artists` | Create a canonical artist. |
| `GET` | `/artists/{artistId}` | Fetch an artist by UUID. |
| `PATCH` | `/artists/{artistId}` | Update an artist primary display name. |
| `POST` | `/artists/{artistId}/aliases` | Add an alias to an artist. |
| `GET` | `/artists/{artistId}/aliases` | List aliases for an artist. |
| `POST` | `/artists/{artistId}/tracks` | Add a track to an artist catalogue. |
| `GET` | `/artists/{artistId}/tracks?page=0&size=50` | List tracks for an artist with bounded pagination. |
| `GET` | `/homepage/artist-of-the-day` | Fetch the deterministic Artist of the Day. |

Example request bodies:

```json
{ "primaryName": "Massive Attack" }
```

```json
{ "alias": "Massive" }
```

```json
{
  "title": "Teardrop",
  "genre": "Trip hop",
  "lengthSeconds": 330,
  "isrc": "GBBKS9800168"
}
```

Swagger UI is the easiest way to explore the full request and response shapes locally.

## 5. Running locally with Gradle + Docker Compose PostgreSQL

Start PostgreSQL only:

```bash
docker compose up -d postgres
```

Run the application from Gradle using the `local` Spring profile:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

The `local` profile defaults to the Docker Compose PostgreSQL settings:

- `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/music_metadata`
- `SPRING_DATASOURCE_USERNAME=music`
- `SPRING_DATASOURCE_PASSWORD=music`

These values can still be overridden with environment variables when needed.

Run the full build and test gate locally:

```bash
./gradlew clean build --no-daemon
```

## 6. Running full app + PostgreSQL with Docker Compose

Build and run the Spring Boot application and PostgreSQL together:

```bash
docker compose up --build
```

The Compose `app` service uses the `prod` Spring profile and supplies datasource environment variables pointing at the Compose PostgreSQL service. These are local development defaults only, not production secrets.

Stop the stack:

```bash
docker compose down
```

The Docker image build intentionally runs `./gradlew clean bootJar --no-daemon` to package the runnable application. CI owns the full `./gradlew clean build --no-daemon` quality gate, including Checkstyle and Testcontainers-backed tests.

## 7. Testing and CI

Local quality gate:

```bash
./gradlew clean build --no-daemon
```

This runs:

- Java compilation
- Checkstyle
- unit and integration tests
- Spring Boot application context checks
- Testcontainers-backed PostgreSQL integration tests

CI is configured with GitHub Actions for pull requests and pushes to `main`. The workflow uses Java 25 and runs the same Gradle clean build command. Dependabot is configured for weekly Gradle dependency and GitHub Actions update checks.

## 8. Observability / useful URLs

When the app is running on the default port:

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- Actuator health: <http://localhost:8080/actuator/health>
- Liveness probe: <http://localhost:8080/actuator/health/liveness>
- Readiness probe: <http://localhost:8080/actuator/health/readiness>
- Prometheus metrics: <http://localhost:8080/actuator/prometheus>

The common and `prod` configurations expose only `health`, `info`, and `prometheus`. The `local` profile also exposes `metrics` and shows health details for easier development troubleshooting.

## 9. Architecture summary

The application is a package-by-feature modular monolith:

```text
com.iceservices.musicmetadata
├── artist      # Artist and ArtistAlias domain, repository, service, API DTOs/controllers
├── track       # Track domain, repository, service, API DTOs/controllers
├── homepage    # Artist of the Day use case and homepage API
└── common      # shared API link type, error handling, time configuration
```

Each feature follows a simple controller → service → repository flow, with PostgreSQL as the durable store. JPA entities are kept behind the API layer and are not exposed directly from controllers; request and response records define the API contract.

The database model is deliberately focused:

- `Artist`: stable canonical identity and primary display name
- `ArtistAlias`: alternate names for the same artist identity
- `Track`: track metadata belonging to exactly one artist

Flyway owns schema creation and Hibernate is configured to validate rather than auto-create the database schema.

## 10. Key decisions / trade-offs

- **Modular monolith over microservices**: the domain is small and tightly related, so a single deployable keeps the solution understandable and operable for the take-home scope.
- **Explicit alias records**: aliases are realistic catalogue metadata and some artists may have many aliases, so they are modelled separately instead of as a string column on `Artist`.
- **Aliases excluded from Artist of the Day**: the rotation is over canonical artists only, preventing artists with more aliases from receiving extra weighting.
- **Deterministic Artist of the Day**: selection uses UTC date, an injectable `Clock`, stable ordering, and modulo rotation. This is fair, repeatable, and testable without introducing a scheduler for the take-home implementation.
- **Offset pagination for tracks**: simple Spring Data pagination is sufficient here and prevents unbounded catalogue responses. Cursor/keyset pagination is documented as a future option for very large catalogues.
- **Flyway over Hibernate auto-DDL**: schema changes are explicit, reviewable, and repeatable across environments.
- **Problem Details-style errors**: common failure cases return structured error bodies rather than ad hoc strings.
- **Docker image builds package only**: the image build uses `bootJar` for speed and reproducibility, while CI is the authoritative full clean build/test gate.

More detailed reasoning is captured in `docs/ARCHITECTURE.md`, `docs/DECISIONS.md`, and `docs/PRODUCTION_READINESS.md`.

## 11. Production-readiness notes

Implemented production-minded foundations:

- Flyway migrations and schema validation
- PostgreSQL-backed persistence
- environment-specific configuration with no committed production credential fallbacks in `prod`
- validation at the API boundary
- Problem Details-style error handling
- Actuator health/probes and Prometheus metrics
- Dockerfile and Docker Compose local runtime support
- GitHub Actions CI running the full Gradle build
- Testcontainers integration tests against PostgreSQL
- Checkstyle for lightweight code hygiene
- Dependabot update automation

Notable production considerations intentionally left for future work:

- authentication and authorization for write operations
- managed secrets and production deployment infrastructure
- rate limiting and edge protection
- alerting, dashboards, tracing, and runbooks
- precomputing or caching Artist of the Day for very high read traffic
- cursor/keyset pagination for extremely large catalogues
- search infrastructure for fuzzy artist, alias, or track discovery

## 12. Future improvements / deliberate non-goals

Deliberate non-goals for this submission:

- no authentication or user accounts
- no frontend; Swagger UI is used for API exploration
- no Redis or distributed cache
- no OpenSearch/Elasticsearch
- no Kafka/SNS/SQS or event-driven integration
- no Kubernetes or infrastructure-as-code deployment setup
- no Spring AI
- no complex copyright, work, rightsholder, or royalty modelling

Reasonable future improvements, depending on product needs and traffic profile:

- OAuth2/OIDC resource-server support and scopes for write APIs
- daily cached or precomputed Artist of the Day
- cursor/keyset pagination for high-volume catalogues
- fuzzy search across artists, aliases, and tracks
- metadata change events for downstream consumers
- production deployment on managed compute with managed PostgreSQL
- SLOs, dashboards, alerts, tracing, and incident runbooks
