package com.jadelomeiri.documentgenerator.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class GeneratedDocumentNotFoundExceptionTests {

	@Test
	void capturesDocumentIdWhenDocumentLookupFails() {
		UUID documentId = UUID.fromString("30000000-0000-0000-0000-000000000001");

		GeneratedDocumentNotFoundException exception = new GeneratedDocumentNotFoundException(documentId);

		assertThat(exception.getDocumentId()).isEqualTo(documentId);
		assertThat(exception.getGenerationRequestId()).isNull();
		assertThat(exception).hasMessage("Generated document not found: " + documentId);
	}

	@Test
	void capturesGenerationRequestIdWhenRequestDocumentLookupFails() {
		UUID requestId = UUID.fromString("40000000-0000-0000-0000-000000000001");

		GeneratedDocumentNotFoundException exception = GeneratedDocumentNotFoundException.forGenerationRequest(requestId);

		assertThat(exception.getDocumentId()).isNull();
		assertThat(exception.getGenerationRequestId()).isEqualTo(requestId);
		assertThat(exception).hasMessage("Generated document not found for generation request: " + requestId);
	}
}
