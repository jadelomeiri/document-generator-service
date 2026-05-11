package com.jadelomeiri.documentgenerator.generation;

import java.util.UUID;

public class GenerationRequestNotFoundException extends RuntimeException {

	private final UUID requestId;

	public GenerationRequestNotFoundException(UUID requestId) {
		super("Document generation request not found: " + requestId);
		this.requestId = requestId;
	}

	public UUID getRequestId() {
		return requestId;
	}
}
