CREATE TABLE document_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT document_templates_name_not_blank CHECK (length(btrim(name)) > 0)
);

CREATE TABLE document_template_versions (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL,
    version_number INTEGER NOT NULL,
    format VARCHAR(10) NOT NULL,
    template_location VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    activated_at TIMESTAMPTZ,
    CONSTRAINT document_template_versions_template_fk
        FOREIGN KEY (template_id) REFERENCES document_templates (id) ON DELETE CASCADE,
    CONSTRAINT document_template_versions_version_number_positive CHECK (version_number > 0),
    CONSTRAINT document_template_versions_format_check CHECK (format IN ('PDF', 'DOCX')),
    CONSTRAINT document_template_versions_status_check CHECK (status IN ('DRAFT', 'ACTIVE', 'RETIRED')),
    CONSTRAINT document_template_versions_location_not_blank CHECK (length(btrim(template_location)) > 0)
);

CREATE TABLE document_generation_requests (
    id UUID PRIMARY KEY,
    template_version_id UUID NOT NULL,
    customer_reference VARCHAR(255) NOT NULL,
    requested_by VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    input_payload_json TEXT NOT NULL,
    failure_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT document_generation_requests_template_version_fk
        FOREIGN KEY (template_version_id) REFERENCES document_template_versions (id),
    CONSTRAINT document_generation_requests_customer_reference_not_blank CHECK (length(btrim(customer_reference)) > 0),
    CONSTRAINT document_generation_requests_requested_by_not_blank CHECK (length(btrim(requested_by)) > 0),
    CONSTRAINT document_generation_requests_status_check CHECK (status IN ('RECEIVED', 'VALIDATED', 'GENERATING', 'COMPLETED', 'FAILED')),
    CONSTRAINT document_generation_requests_input_payload_json_not_blank CHECK (length(btrim(input_payload_json)) > 0)
);

CREATE TABLE generated_documents (
    id UUID PRIMARY KEY,
    generation_request_id UUID NOT NULL UNIQUE,
    template_version_id UUID NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    checksum VARCHAR(128),
    storage_reference VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT generated_documents_generation_request_fk
        FOREIGN KEY (generation_request_id) REFERENCES document_generation_requests (id) ON DELETE CASCADE,
    CONSTRAINT generated_documents_template_version_fk
        FOREIGN KEY (template_version_id) REFERENCES document_template_versions (id),
    CONSTRAINT generated_documents_content_type_not_blank CHECK (length(btrim(content_type)) > 0),
    CONSTRAINT generated_documents_storage_reference_not_blank CHECK (length(btrim(storage_reference)) > 0)
);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_id UUID NOT NULL,
    actor_reference VARCHAR(255) NOT NULL,
    details_json TEXT,
    occurred_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT audit_events_event_type_not_blank CHECK (length(btrim(event_type)) > 0),
    CONSTRAINT audit_events_target_type_not_blank CHECK (length(btrim(target_type)) > 0),
    CONSTRAINT audit_events_actor_reference_not_blank CHECK (length(btrim(actor_reference)) > 0)
);

CREATE INDEX document_template_versions_template_id_idx ON document_template_versions (template_id, version_number);
CREATE UNIQUE INDEX document_template_versions_template_id_version_number_unique_idx
    ON document_template_versions (template_id, version_number);
CREATE INDEX document_generation_requests_template_version_id_idx ON document_generation_requests (template_version_id);
CREATE INDEX generated_documents_template_version_id_idx ON generated_documents (template_version_id);
CREATE INDEX audit_events_target_type_target_id_occurred_at_idx ON audit_events (target_type, target_id, occurred_at);

INSERT INTO document_templates (id, name, description, active, created_at, updated_at) VALUES
    ('10000000-0000-0000-0000-000000000001', 'Loan Agreement', 'Demo loan agreement template for generated lending documents.', true,
        '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
    ('10000000-0000-0000-0000-000000000002', 'Customer Statement', 'Demo customer statement template for account communications.', true,
        '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z');

INSERT INTO document_template_versions (
    id, template_id, version_number, format, template_location, status, created_at, activated_at
) VALUES
    ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 1, 'PDF',
        'classpath:/demo-templates/loan-agreement-v1.pdf', 'ACTIVE', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 1, 'PDF',
        'classpath:/demo-templates/customer-statement-v1.pdf', 'ACTIVE', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z');
