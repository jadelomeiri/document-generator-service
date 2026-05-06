# Architecture

## Overview

The service is a single Spring Boot application using a layered architecture:

Controller -> Service -> Repository -> PostgreSQL

The implementation is intentionally a modular monolith. The current domain is small and tightly related, so splitting it into microservices would add operational complexity without a clear benefit.

## Main Components

- Artist API: create artists, update primary names, manage aliases
- Track API: add tracks and fetch tracks for an artist
- Homepage API: return Artist of the Day
- Persistence: PostgreSQL with Flyway migrations
- API documentation: OpenAPI / Swagger
- Observability: Spring Boot Actuator and Prometheus metrics endpoint

## Data Model

### Artist

Represents stable artist identity.

### ArtistAlias

Represents alternative names for the same artist identity.

Aliases are not separate artists and do not participate in Artist of the Day rotation.

### Track

Represents track metadata belonging to an artist.

## Artist of the Day

The daily artist is selected using deterministic rotation over canonical artists only.

Aliases are deliberately excluded from the rotation so that artists with many aliases are not overrepresented.

The take-home implementation uses:

1. Canonical artists sorted by `created_at ASC, id ASC`
2. UTC date from an injectable `Clock`
3. A fixed epoch date
4. `daysSinceEpoch % artistCount` to select the daily artist

This avoids random selection and keeps the endpoint deterministic: the same catalogue and UTC date produce the same Artist of the Day.

The implementation calculates the selection on request using a count plus a one-row sorted lookup rather than loading the full catalogue into memory.

In a full production system, this would likely be precomputed or cached once per UTC day so the homepage endpoint remains a cheap read and the daily artist cannot change during the day if artists are added.

## Data Model Assumptions

The model separates artist identity from artist names.

This matters because artist names are not always stable. An artist may perform under multiple aliases, change their primary display name, or have one-off aliases used for specific releases.

The service therefore uses:

- `Artist` as the stable canonical identity
- `ArtistAlias` as alternate names linked to that identity
- `Track` as metadata linked to the stable artist ID

Aliases are deliberately not treated as separate artists. This prevents duplicate catalogues and avoids unfairly weighting artists with many aliases in the Artist of the Day rotation.

Track retrieval is paginated because some artists may have very large catalogues. Even though the requirement says to fetch tracks for an artist, the API should avoid unbounded responses in a customer-facing service.

## API Scope

The API includes a small set of artist endpoints even though the task only explicitly mentions editing an artist name.

This is intentional. Editing an artist name and adding tracks to an artist catalogue require a stable artist resource. Creating and retrieving that artist makes the service usable, testable, and easier to reason about.

The API does not attempt to become a full catalogue management platform. It only includes endpoints that support the required behaviours and the alias model.

## Production Deployment Considerations

For production, the natural deployment target would be a containerised Spring Boot service on AWS ECS/Fargate behind a load balancer, backed by managed PostgreSQL.

Future production additions could include:

- Redis/CDN caching for hot reads and Artist of the Day
- OpenSearch for fuzzy artist, alias, and track search
- SNS/SQS or Kafka for asynchronous metadata events
- Read replicas for high-volume read traffic
- OAuth2/OIDC for write operation security
- OpenTofu for infrastructure provisioning

These are documented as future evolutions rather than implemented in the take-home scope.