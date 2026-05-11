# Demo Script

A short path for running the LDMS interview demo locally.

## 1. Build

```bash
./gradlew clean build
```

## 2. Start PostgreSQL

```bash
docker compose up -d postgres
```

## 3. Run the app

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## 4. List templates

```bash
curl http://localhost:8080/api/v1/templates
```

## 5. List Loan Agreement versions

```bash
curl http://localhost:8080/api/v1/templates/10000000-0000-0000-0000-000000000001/versions
```

## 6. Create a generation request

```bash
curl -X POST http://localhost:8080/api/v1/generation-requests \
  -H 'Content-Type: application/json' \
  -d '{
    "templateVersionId": "20000000-0000-0000-0000-000000000001",
    "customerReference": "customer-123",
    "requestedBy": "caseworker-456",
    "inputPayloadJson": "{\"loanAmount\":125000,\"currency\":\"GBP\"}"
  }'
```

Expected result:

- `status` is `COMPLETED`.
- `generatedDocument` is present.
- `generatedDocument.templateVersionId` matches `20000000-0000-0000-0000-000000000001`.
- `generatedDocument.storageReference` starts with `demo://generated-documents/`.
- Response includes links to the request and audit events.

## 7. View audit events

```bash
curl http://localhost:8080/api/v1/generation-requests/{requestId}/audit-events
```

Expected audit events:

- `GENERATION_REQUEST_CREATED`
- `GENERATION_COMPLETED`
