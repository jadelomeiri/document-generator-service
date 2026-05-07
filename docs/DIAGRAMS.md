# Architecture Diagrams and Presentation Notes

These diagrams are reviewer aids for the current take-home implementation. Future infrastructure is labelled explicitly and is not implemented in this repository.

## Current application architecture

```mermaid
flowchart TB
    client["Client / Swagger UI"] --> rest["Spring Boot REST API\nSpring Web MVC + validation + Problem Details"]

    rest --> artistApi["Artist API\n/api/v1/artists"]
    rest --> trackApi["Track API\n/api/v1/artists/{artistId}/tracks"]
    rest --> homepageApi["Homepage / Artist of the Day API\n/api/v1/homepage/artist-of-the-day"]
    rest --> actuator["Actuator / Prometheus\n/actuator/health\n/actuator/prometheus"]

    artistApi --> artistService["ArtistService"]
    trackApi --> trackService["TrackService"]
    homepageApi --> aotdService["ArtistOfTheDayService\nUTC Clock + deterministic rotation"]

    artistService --> artistRepo["ArtistRepository"]
    artistService --> aliasRepo["ArtistAliasRepository"]
    trackService --> trackRepo["TrackRepository"]
    trackService --> artistRepo
    aotdService --> artistRepo

    artistRepo --> postgres[("PostgreSQL")]
    aliasRepo --> postgres
    trackRepo --> postgres

    flyway["Flyway migrations\nsrc/main/resources/db/migration"] --> postgres

    subgraph packages["Source package structure"]
        artistPkg["artist + artist/api"]
        trackPkg["track + track/api"]
        homepagePkg["homepage + homepage/api"]
        commonPkg["common/api, common/error, common/time"]
    end
```

## Local runtime and CI/testing setup

```mermaid
flowchart LR
    dev["Developer"] --> gradle["Gradle build\n./gradlew clean build --no-daemon"]
    gradle --> unit["Unit/service tests\nJUnit 5"]
    gradle --> mvc["MockMvc integration tests"]
    mvc --> tcpg[("Testcontainers PostgreSQL")]
    gradle --> checkstyle["Checkstyle"]
    gradle --> bootJar["Spring Boot package"]

    dev --> compose["Docker Compose"]
    compose --> app["app service\nSpring Boot prod profile\nhttp://localhost:8080"]
    compose --> localPg[("postgres service\nPostgreSQL 18\nlocalhost:5432")]
    app --> localPg

    app --> swagger["Swagger UI\n/swagger-ui/index.html"]
    app --> health["Actuator URLs\n/actuator/health\n/actuator/health/liveness\n/actuator/health/readiness"]
    app --> prom["Prometheus metrics\n/actuator/prometheus"]

    gha["GitHub Actions\nPRs + pushes to main"] --> setupJava["Java 25 + Gradle setup"]
    setupJava --> ciBuild["./gradlew clean build --no-daemon"]
    ciBuild --> unit
    ciBuild --> mvc
    ciBuild --> checkstyle
```

## Future production architecture (not implemented)

```mermaid
flowchart TB
    users["Users / API clients"] --> alb["Future: Application Load Balancer"]
    alb --> runtime["Future: ECS/Fargate or equivalent\ncontainer runtime"]

    runtime --> rds[("Future: RDS PostgreSQL")]
    runtime --> secrets["Future: Secrets Manager\ndatasource credentials"]
    runtime --> metrics["Future: metrics/logging\nPrometheus-compatible metrics\ncentralised logs and alerts"]

    gha["GitHub Actions"] --> build["Build + test\n./gradlew clean build --no-daemon"]
    build --> image["Build container image"]
    image --> ecr["Future: ECR"]
    ecr --> runtime

    redis["Future optional: Redis\nArtist of the Day / hot-read cache"]
    search["Future optional: OpenSearch\nfuzzy artist, alias, track search"]
    messaging["Future optional: SNS/SQS or Kafka\nmetadata change events"]

    runtime -.-> redis
    runtime -.-> search
    runtime -.-> messaging

    classDef future fill:#fff7ed,stroke:#f97316,color:#7c2d12,stroke-dasharray: 5 5;
    class alb,runtime,rds,secrets,metrics,ecr,redis,search,messaging future;
```

## 15-minute presentation outline

1. **Problem understanding (1 minute)**
   - Build a pragmatic music metadata API for canonical artists, aliases, tracks, and a homepage Artist of the Day.
   - Keep the scope useful for a streaming-platform-like product without adding unrelated platform features.

2. **Domain model (2 minutes)**
   - `Artist` is the stable canonical identity with a primary display name.
   - `ArtistAlias` is explicit metadata linked to an artist, not a separate artist.
   - `Track` belongs to exactly one artist and is retrieved with bounded pagination.
   - Artist of the Day rotates over canonical artists only, so aliases do not skew fairness.

3. **API walkthrough (3 minutes)**
   - Create, fetch, and update artists.
   - Add and list aliases.
   - Add and page through tracks for an artist.
   - Fetch `/api/v1/homepage/artist-of-the-day` and inspect contracts in Swagger UI.
   - Show validation, duplicate handling, and Problem Details-style error responses.

4. **Architecture (3 minutes)**
   - Package-by-feature modular monolith: artist, track, homepage, and common support packages.
   - Controller to service to repository flow keeps the implementation readable.
   - PostgreSQL is the source of truth; Flyway owns schema changes; Hibernate validates only.
   - Actuator and Prometheus endpoints provide basic operational visibility.

5. **Testing and production readiness (2 minutes)**
   - Unit/service tests cover business rules such as alias handling, track normalisation, and Artist of the Day determinism.
   - MockMvc and Testcontainers PostgreSQL tests cover real API and database behaviour.
   - GitHub Actions runs the same clean Gradle build as the local quality gate.
   - Docker Compose supports local app plus PostgreSQL runtime checks.

6. **Trade-offs (2 minutes)**
   - Modular monolith instead of microservices because the domain is small and tightly related.
   - Page-based pagination is simple and bounded; cursor pagination is a future option for very large catalogues.
   - Artist of the Day is computed on request for simplicity; production could precompute or cache once per UTC day.
   - Authentication, search, messaging, and distributed caching are intentionally out of take-home scope.

7. **Next improvements (2 minutes)**
   - Add OAuth2/OIDC and scope-protected write operations.
   - Add a production deployment path such as ECR, ECS/Fargate, RDS, Secrets Manager, dashboards, and alerts.
   - Add read optimisation where justified: daily cache/precompute, read replicas, or cursor pagination.
   - Add OpenSearch or messaging only when product needs or integration consumers require them.
