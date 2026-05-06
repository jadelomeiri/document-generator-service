# Decision Log

## 1. Use Java 25 and Spring Boot 4

This is a greenfield service, so I chose Java 25 as the current LTS JVM and Spring Boot 4 as the latest Spring Boot generation.

The main reason for choosing Spring Boot is alignment with ICE's technology stack. For a take-home task, I wanted the implementation to be easy for the team to review and close to the environment the role is focused on.

Alternatives considered:

- Groovy and Grails: this would probably let me move faster personally, and could lead to a smaller, very readable implementation. I have experience with Grails and like its productivity for CRUD-heavy applications. I chose not to use it here because the role and copyright product stack are Spring Boot based, and I wanted the solution to demonstrate relevant Spring engineering judgement.
- Micronaut: a good JVM alternative, especially if the primary deployment target were serverless or very lightweight cloud functions. I did not choose it because the submitted solution is designed as a containerised service rather than a Lambda-first application.
- Kotlin and Spring Boot: also a strong option for concise JVM services. I stayed with Java because it is the most universally reviewable choice for a Java/Spring-focused interview process.

The decision is not based on blindly matching the employer's stack. If another tool were clearly better for the problem, I would choose it. In this case, Spring Boot is both suitable for the problem and aligned with the target engineering environment.

## 2. Use PostgreSQL as the source of truth

Artists, aliases, and tracks are relational data.

PostgreSQL gives strong consistency, constraints, indexes, and straightforward querying for the current requirements.

Alternatives considered:

- DynamoDB: good for high-scale key-value access, but less natural for this relational model.
- MongoDB: flexible, but unnecessary for this structured metadata.
- OpenSearch: useful for search, but not as the source of truth.

## 3. Model artist aliases explicitly

The task calls out artists having multiple aliases.

Instead of treating artist name changes as simple overwrites, aliases are modelled as records linked to a stable artist ID.

This preserves artist identity even when names change.

## 4. Use deterministic Artist of the Day rotation

Random selection does not guarantee fair rotation.

A deterministic modulo-based approach ensures each artist appears once per cycle.

Aliases are excluded from rotation so artists with more aliases are not overrepresented.

## 5. Use pagination for artist tracks

The requirement says fetch tracks for an artist. In a global-scale service, unbounded responses are risky.

The API returns paginated tracks to keep response sizes predictable.

## 6. Use a modular monolith for the take-home

The service is implemented as one deployable Spring Boot application with clear internal package boundaries.

Microservices are not justified by the current scope and would introduce network, deployment, and consistency complexity too early.

## 7. Use lightweight HATEOAS for API discoverability

The task mentions a user-friendly interface for accessing metadata. I chose not to build a full frontend because this is primarily a backend take-home task.

Instead, API responses include lightweight HATEOAS links to related actions such as fetching tracks, adding tracks, and managing aliases.

This improves API discoverability without adding the cost of a separate UI.

## 8. Do not implement authentication in the take-home scope

Authentication is important in production, especially for write operations.

For this exercise, it is documented as a future production concern rather than implemented, to keep the focus on the domain, API, testing, and design.

In production, write operations would likely be protected through OAuth2/OIDC using the platform identity provider.

## 9. Provide a small Artist API around the required behaviours

The task explicitly requires editing an artist name and adding tracks to an artist catalogue. To support those behaviours cleanly, the service needs a way to create and retrieve artists as stable resources.

I therefore added a small Artist API:

- `POST /api/v1/artists` to create an artist
- `GET /api/v1/artists/{artistId}` to retrieve an artist
- `PATCH /api/v1/artists/{artistId}` to edit the artist primary name

This is not intended to expand the scope into a full artist management system. It is the minimal API needed to make the required user experiences usable and testable.

## 10. Return aliases as a bounded list but paginate tracks

Alias listing is returned as a simple bounded list because alias counts are expected to be small compared with track catalogues.

The working assumption is that most artists have a small number of aliases, while edge cases may have dozens. That is still a very different scale from tracks, where prolific artists can have hundreds or thousands of entries.

Track retrieval is therefore paginated, while alias listing is kept simple for readability and usability.

## 11. Use lightweight HATEOAS rather than full hypermedia modelling

I used lightweight HATEOAS links rather than fully modelling responses around Spring HATEOAS `RepresentationModel` or `EntityModel`.

The goal was to improve API discoverability without making hypermedia the focus of the take-home.

The implementation keeps `_links` in responses and uses Spring HATEOAS link building where useful, but avoids adding extra framework ceremony where explicit response records are easier to read and explain.

## 12. Treat aliases as unique per artist, not globally unique

The same artist cannot have the same alias twice. This is enforced case-insensitively per artist.

Different artists can theoretically have the same alias or display name. Real-world music metadata can be messy, and global uniqueness on artist names or aliases would be too strict for this task.

This is why the unique alias constraint is scoped to `(artist_id, lower(alias_name))` rather than `lower(alias_name)` globally.

## 13. Keep genre optional

Music genres are messy, subjective, and often multi-valued.

For this task, I modelled `genre` as an optional simple string rather than trying to build a full genre taxonomy or many-to-many genre model.

The API should avoid storing blank genre values. If a blank genre is submitted, it should be normalised to `null`.

## 14. Validate at the API layer and protect at the database layer

Request validation should happen at the API boundary so clients get clear, friendly errors.

The database still enforces important invariants such as non-blank names, positive track length, valid ISRC format, and uniqueness constraints.

This gives a better developer experience while still protecting data integrity if bad data reaches the persistence layer.