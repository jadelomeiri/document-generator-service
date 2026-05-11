package com.jadelomeiri.documentgenerator.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
		UUID id,
		String eventType,
		String targetType,
		UUID targetId,
		String actorReference,
		String detailsJson,
		Instant occurredAt) {
}
