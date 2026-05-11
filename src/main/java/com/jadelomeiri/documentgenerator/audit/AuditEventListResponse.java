package com.jadelomeiri.documentgenerator.audit;

import java.util.List;

public record AuditEventListResponse(List<AuditEventResponse> auditEvents) {
}
