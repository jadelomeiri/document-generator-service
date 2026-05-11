package com.jadelomeiri.documentgenerator.document;

import java.time.Instant;
import java.util.UUID;

public record GeneratedDocumentResponse(
		UUID id,
		UUID generationRequestId,
		UUID templateVersionId,
		String contentType,
		String checksum,
		String storageReference,
		Instant createdAt) {
}
