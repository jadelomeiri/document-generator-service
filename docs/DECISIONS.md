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

The service needs to return one Artist of the Day in a way that is fair, predictable, and easy to test.

For the take-home, I chose a deterministic daily rotation:

- Sort canonical artists by `created_at ASC, id ASC`
- Calculate the number of UTC days since a fixed epoch date
- Select `daysSinceEpoch % artistCount`

This means the same artist is returned for all users on the same UTC day, aliases do not increase an artist's chance of being selected, and the behaviour can be tested by injecting a `Clock`.

### Why modulo rotation

Modulo rotation lets the service derive the selected artist from the date without storing a mutable pointer.

Given a stable sorted list of artists, `daysSinceEpoch % artistCount` naturally cycles through the catalogue and wraps back to the first artist after the last artist.

For example, with five artists:

- day 0 selects index 0
- day 1 selects index 1
- day 2 selects index 2
- day 3 selects index 3
- day 4 selects index 4
- day 5 wraps back to index 0

This keeps the take-home implementation deterministic, fair, stateless, and easy to reason about.

### Alternatives considered

#### Random selection per request

This is simple, but not stable or fair. Different users could see different artists on the same day, and some artists could be selected repeatedly while others are skipped.

I rejected this because the requirement is for a cyclical Artist of the Day, not a random artist recommendation.

#### Random selection once per day

This would be stable for a day if stored, but it still would not guarantee fair rotation through the catalogue.

I rejected this because deterministic rotation better matches the fairness requirement.

#### Stored pointer / simple iteration

Another option would be to store the last selected artist and move to the next artist each day.

I did not choose this for the take-home because it introduces mutable state, scheduling, and concurrency questions: where the pointer is stored, what happens if the job fails, and how multiple app instances coordinate updates.

Modulo rotation avoids those concerns by deriving the selected artist from the date.

#### Precomputed daily selection

A production system could store the selected artist for each UTC date in an `artist_of_the_day` table.

This would make the homepage endpoint a cheap read and would prevent today's selected artist changing if artists are added later in the day.

I did not implement this in the take-home because it adds scheduling and persistence complexity beyond the core requirement. I would treat it as the next production improvement.

#### Weighted or editorial selection

A real streaming platform might eventually choose artists based on popularity, territory, genre, campaigns, or editorial rules.

I rejected this for the take-home because it changes the requirement from fair cyclical rotation into a product recommendation or promotion problem.

### Handling newly added artists

With the take-home implementation, newly added artists can change the modulo calculation because the total artist count changes.

That means today's selected artist could theoretically change if a new artist is added during the same UTC day.

That is acceptable for this time-boxed implementation, but in production I would avoid the selected artist changing during the UTC day by storing the daily selection once computed.

New artists would then enter the rotation from the next computed day onward.

### Caching decision

I did not add Redis or a distributed cache in the take-home implementation because that would add infrastructure complexity beyond the core task.

In production, this endpoint is a strong candidate for daily caching or precomputation because the result is the same for every user on the same UTC day.

A production version could store the selected artist for each date in an `artist_of_the_day` table or cache the result until the next UTC midnight.

## 5. Use page-based pagination for artist tracks

The requirement says fetch tracks for an artist. In a global-scale service, unbounded responses are risky.

The API returns page-based paginated tracks to keep response sizes predictable and to keep the first implementation easy for clients to understand: `page=0&size=50` is explicit, simple to test, and works well for the current catalogue-management use case.

Cursor or keyset pagination would be a better fit for very large catalogues or high-write traffic because it avoids deep-offset scans and is more stable when rows are inserted while a client is paging. I have not implemented it in this take-home because it would add API and query complexity before the simpler page-based approach has become a proven bottleneck. It is documented as a future P2 scalability improvement.

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