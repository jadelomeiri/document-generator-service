# Diagrams

These diagrams describe the first implemented Document Generator Service backend slice and the future seams that remain deliberately out of scope.

## System context

```mermaid
flowchart LR
    user["Lending or operations user"] --> frontend["Frontend flow\n(documented only)"]
    frontend --> api["Document Generator Service\nSpring Boot REST API"]
    api --> db[("PostgreSQL\nsource of truth")]
    api --> renderer["Generation boundary\nfirst slice: simple metadata producer"]
    renderer -. "future" .-> realRenderer["PDF/DOCX renderer"]
    api -. "future" .-> storage["Object storage"]
```

## Backend components

```mermaid
flowchart TB
    rest["REST API"] --> templateApi["Template API"]
    rest --> requestApi["Generation Request API"]
    rest --> documentApi["Generated Document Metadata API"]
    rest --> auditApi["Audit API"]

    templateApi --> templateService["Template Service"]
    requestApi --> requestService["Generation Request Service"]
    documentApi --> documentService["Generated Document Metadata Service"]
    auditApi --> auditService["Audit Service"]

    requestService --> generationBoundary["Generation Boundary"]
    requestService --> auditService
    templateService --> auditService
    documentService --> auditService

    templateService --> templateRepo["Template Repository"]
    requestService --> requestRepo["Generation Request Repository"]
    documentService --> documentRepo["Generated Document Repository"]
    auditService --> auditRepo["Audit Event Repository"]

    templateRepo --> postgres[("PostgreSQL")]
    requestRepo --> postgres
    documentRepo --> postgres
    auditRepo --> postgres
```

## Core data relationships

The core model uses five first-class concepts: document templates, template versions, generation requests, generated document metadata, and audit events.

```mermaid
erDiagram
    DOCUMENT_TEMPLATE ||--o{ DOCUMENT_TEMPLATE_VERSION : has
    DOCUMENT_TEMPLATE_VERSION ||--o{ GENERATION_REQUEST : used_by
    GENERATION_REQUEST ||--o| GENERATED_DOCUMENT : produces
    GENERATION_REQUEST ||--o{ AUDIT_EVENT : records
    DOCUMENT_TEMPLATE ||--o{ AUDIT_EVENT : records
    DOCUMENT_TEMPLATE_VERSION ||--o{ AUDIT_EVENT : records
    GENERATED_DOCUMENT ||--o{ AUDIT_EVENT : records

    DOCUMENT_TEMPLATE {
        uuid id
        string name
        string description
        timestamp created_at
        timestamp updated_at
    }

    DOCUMENT_TEMPLATE_VERSION {
        uuid id
        uuid template_id
        int version_number
        string status
        timestamp created_at
        timestamp activated_at
    }

    GENERATION_REQUEST {
        uuid id
        uuid template_version_id
        string status
        string requester_reference
        timestamp created_at
        timestamp updated_at
        string failure_reason
    }

    GENERATED_DOCUMENT {
        uuid id
        uuid generation_request_id
        string content_type
        string checksum
        string storage_reference
        timestamp created_at
    }

    AUDIT_EVENT {
        uuid id
        string event_type
        string target_type
        uuid target_id
        string actor_reference
        timestamp occurred_at
    }
```

## Generation request lifecycle

```mermaid
stateDiagram-v2
    [*] --> RECEIVED
    RECEIVED --> VALIDATED: request passes validation
    RECEIVED --> FAILED: request cannot be accepted
    VALIDATED --> GENERATING: generation starts
    GENERATING --> COMPLETED: metadata stored
    GENERATING --> FAILED: generation fails
    COMPLETED --> [*]
    FAILED --> [*]
```

## First-slice sequence

```mermaid
sequenceDiagram
    participant UI as Frontend flow
    participant API as REST API
    participant DB as PostgreSQL
    participant GEN as Generation boundary

    UI->>API: POST generation request
    API->>API: Validate request DTO
    API->>DB: Insert generation request
    API->>DB: Insert audit event: request created
    API->>DB: Update status to GENERATING
    API->>GEN: Generate document metadata
    GEN-->>API: Metadata result
    API->>DB: Insert generated document metadata
    API->>DB: Update status to COMPLETED
    API->>DB: Insert audit event: completed
    API-->>UI: Generation request response
```

## Future production extensions

```mermaid
flowchart LR
    api["Document Generator Service"]
    api -. "future async processing" .-> queue["Queue or job worker"]
    queue -.-> renderer["Rendering service"]
    renderer -.-> storage["Object storage"]
    api -. "future notifications" .-> webhook["Webhook/callback"]
    api -. "future search" .-> search["Search index"]
```

These extensions are intentionally not part of the first implementation slice. They are useful discussion points only after the core lifecycle is implemented and tested.
