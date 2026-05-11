package com.jadelomeiri.documentgenerator.template;

import com.jadelomeiri.documentgenerator.common.api.LinkResponse;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record DocumentTemplateResponse(
		UUID id,
		String name,
		String description,
		boolean active,
		Instant createdAt,
		Instant updatedAt,
		Map<String, LinkResponse> links) {
}
