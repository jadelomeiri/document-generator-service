package com.jadelomeiri.documentgenerator.document;

import java.util.UUID;

public class GeneratedDocumentNotFoundException extends RuntimeException {

	private final UUID documentId;
	private final UUID generationRequestId;

	public GeneratedDocumentNotFoundException(UUID documentId) {
		super("Generated document not found: " + documentId);
		this.documentId = documentId;
		this.generationRequestId = null;
	}

	private GeneratedDocumentNotFoundException(UUID generationRequestId, String message) {
		super(message);
		this.documentId = null;
		this.generationRequestId = generationRequestId;
	}

	public static GeneratedDocumentNotFoundException forGenerationRequest(UUID generationRequestId) {
		return new GeneratedDocumentNotFoundException(
				generationRequestId,
				"Generated document not found for generation request: " + generationRequestId);
	}

	public UUID getDocumentId() {
		return documentId;
	}

	public UUID getGenerationRequestId() {
		return generationRequestId;
	}
}
