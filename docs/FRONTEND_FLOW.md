# Frontend Flow

A frontend is not implemented for this interview demo. This document explains the expected user flow so the backend API can be designed around realistic client behaviour.

The backend remains the source of truth for template versions, generation request status, generated document metadata, and audit history.

## Primary user journey

1. A lending or operations user opens a document-generation screen.
2. The frontend loads available document templates from the backend.
3. The user selects a template, such as a loan agreement, disclosure pack, statement, or offer letter.
4. The frontend displays the active template version and the required input context.
5. The user enters or confirms the business data needed for generation.
6. The frontend submits a generation request to the backend.
7. The backend validates and stores the request, then returns the request id and current status.
8. The frontend shows the request status.
9. When generation completes, the frontend displays generated document metadata and any available open/download action.
10. Operational users can view audit events for the request.

## Screens implied by the backend

### Template list

Purpose: allow a user to choose the document they need to generate.

Backend needs:

- List templates.
- Indicate active version where relevant.
- Avoid exposing implementation details of template storage.

### Template detail

Purpose: show what will be generated and what inputs are required.

Backend needs:

- Fetch template and version metadata.
- Identify whether a version is active or retired.
- Provide enough metadata for the UI to render a sensible form or summary.

### Generation request submission

Purpose: submit a document-generation request.

Backend needs:

- Validate the selected template version.
- Validate required request fields.
- Create a durable generation request record.
- Return a request id and current status.

### Request status view

Purpose: show progress without the UI inventing state.

Backend needs:

- Fetch generation request by id.
- Return status, timestamps, failure reason when applicable, and generated document metadata when available.

### Generated document view

Purpose: show the result of a completed request.

Backend needs:

- Fetch generated document metadata.
- Provide content type, checksum, creation timestamp, and storage/download reference if supported.

The first backend slice can expose metadata only. Real file download can be added later.

### Audit view

Purpose: support operational traceability.

Backend needs:

- List audit events for a generation request or related resource.
- Show event type, timestamp, actor/requester context, and concise event details.
- Paginate audit history.

## Client rules

The frontend should:

- Treat backend status as authoritative.
- Submit generation against an explicit template version when required by the API.
- Display failure reasons returned by the backend in an operationally useful but safe way.
- Avoid caching template or status data in a way that hides backend changes.

The frontend should not:

- Infer completion just because a file exists.
- Mutate request state locally.
- Assume the latest template version was used unless the backend says so.
- Bypass audit history for operational actions.

## Deliberately out of scope

- Building the frontend.
- Authentication screens.
- Design system work.
- Browser-based document editing.
- Live preview rendering.
- Real file download until storage/rendering is implemented.
