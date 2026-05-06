# Task Brief

Build a Music Metadata Service for a streaming-platform-like product.

The service should support:

- Adding a new track to an artist catalogue
- Editing an artist name
- Supporting artists with multiple aliases
- Fetching tracks for an artist
- Showing a fair cyclical Artist of the Day

## Interpretation

The functional requirements are small, but the brief frames the service as customer-facing and serving many users globally.

Therefore, the implementation should avoid toy choices such as:

- Name-based relationships
- Random Artist of the Day selection
- Unbounded track retrieval
- Leaking persistence entities directly through the API
- Undocumented assumptions

## Working Assumptions

### Artist aliases

Most artists are likely to have a small number of aliases, often around 1 to 3. However, some prolific artists, especially in genres such as techno, electronic, or experimental music, may have many more — potentially 30 or more one-off or limited-use aliases.

Because of this, aliases are modelled explicitly in a separate table rather than as a single text field on the artist.

### Artist track catalogues

Most artists may have relatively small catalogues, but prolific artists can have hundreds or thousands of tracks.

Because of this, fetching tracks for an artist should not return an unbounded list. The API uses pagination to keep response sizes predictable.

### Artist identity

Artist names and aliases can change, but the artist identity should remain stable. Tracks are linked to a stable artist ID rather than to an artist name.

### Scope

The service models artists, aliases, and tracks only. It does not attempt to model the full music copyright domain, such as musical works, recordings, contributors, splits, rightsholders, territories, or royalty rules.

## Main Goal

Deliver a small but production-minded service that is easy to run, test, review, and discuss in a technical interview.

The solution should show pragmatic engineering judgement rather than unnecessary cloud or microservice complexity.