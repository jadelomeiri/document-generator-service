# Task Brief: Document Generator Service

## Interview context

This repository is being repurposed for an LDMS Senior Java Engineer final interview whiteboard exercise: **Design a Document Generator**.

The deliverable should be a small, production-minded Spring Boot backend demo. It should be easy to explain in an interview and should demonstrate pragmatic senior engineering choices rather than a large platform build.

## Problem statement

Design and implement the backend for a document generator in a fintech/lending context.

The system should support the lifecycle of creating documents from versioned templates, managing generation requests, storing generated document metadata, and recording audit events. The backend is the source of truth for template versions, request statuses, generated document metadata, and audit history.

## Intended users and flow

A likely frontend flow is:

1. A lending or operations user chooses a document template, such as a loan agreement, disclosure, statement, or offer letter.
2. The UI displays the selected template and version information.
3. The user submits generation inputs for a customer, loan, account, or application context.
4. The backend validates the request and creates a durable generation request.
5. The backend moves the request through a clear status lifecycle.
6. The UI reads request status and, when complete, displays generated document metadata and a link/action if retrieval is supported.
7. Operational users can inspect audit events for traceability.

The frontend itself is out of scope. The flow is documented to show how the backend APIs would be used.

## Core domain concepts

The backend model has five first-class concepts:

- **Document template**: A named template definition for a business document.
- **Template version**: An immutable version of a template used by generation requests.
- **Generation request**: A durable request record to generate a document from a specific template version and payload.
- **Generated document metadata**: Metadata for the produced document, not necessarily the document bytes themselves.
- **Audit event**: Append-only record of important lifecycle events.

## Design priorities

The design should prioritise:

- Auditability and traceability.
- Immutable template versioning.
- Request status management.
- Clear API contracts and validation.
- Problem Details-style error responses.
- Database-backed source of truth.
- Testable business logic, especially state transitions and audit events.
- Small scope that can be explained and evolved during an interview.

## Non-goals

Do not implement these unless explicitly requested later:

- Full frontend.
- Authentication or user account management.
- Real document rendering integration.
- External document storage.
- Message queues or event streaming.
- Distributed cache.
- Search infrastructure.
- Kubernetes or infrastructure-as-code.
- Large workflow engine.

These are valid production topics, but for this exercise they should be treated as future extensions rather than part of the first build.

## Current repository state

The repository now contains the first document-generator backend slice: seeded templates and versions, a synchronous demo generation workflow, generated document metadata, request audit events, Flyway schema, REST APIs, and Testcontainers-backed integration tests.
