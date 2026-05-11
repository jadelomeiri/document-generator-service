package com.jadelomeiri.documentgenerator.document;

import java.util.UUID;

public class GeneratedDocumentNotFoundException extends RuntimeException {

	private final UUID documentId;

	public GeneratedDocumentNotFoundException(UUID documentId) {
		super("Generated document not found: " + documentId);
		this.documentId = documentId;
	}

	public UUID getDocumentId() {
		return documentId;
	}
}
