package com.jadelomeiri.documentgenerator.template;

import java.time.Instant;
import java.util.UUID;

public record DocumentTemplateVersionResponse(
		UUID id,
		UUID templateId,
		int versionNumber,
		DocumentFormat format,
		String templateLocation,
		TemplateVersionStatus status,
		Instant createdAt,
		Instant activatedAt) {
}
