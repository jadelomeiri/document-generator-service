package com.jadelomeiri.documentgenerator.generation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.jadelomeiri.documentgenerator.common.api.LinkResponse;
import com.jadelomeiri.documentgenerator.document.GeneratedDocumentResponse;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GenerationRequestResponse(
		UUID id,
		UUID templateVersionId,
		String customerReference,
		String requestedBy,
		GenerationRequestStatus status,
		String inputPayloadJson,
		String failureReason,
		Instant createdAt,
		Instant updatedAt,
		Instant completedAt,
		GeneratedDocumentResponse generatedDocument,
		Map<String, LinkResponse> links) {
}
