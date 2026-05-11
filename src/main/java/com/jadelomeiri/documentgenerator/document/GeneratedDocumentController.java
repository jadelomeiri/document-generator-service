package com.jadelomeiri.documentgenerator.document;

import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/generated-documents")
public class GeneratedDocumentController {

	private final GeneratedDocumentService generatedDocumentService;

	public GeneratedDocumentController(GeneratedDocumentService generatedDocumentService) {
		this.generatedDocumentService = generatedDocumentService;
	}

	@GetMapping("/{documentId}")
	public GeneratedDocumentResponse getGeneratedDocument(@PathVariable UUID documentId) {
		return toResponse(generatedDocumentService.getGeneratedDocument(documentId));
	}

	public static GeneratedDocumentResponse toResponse(GeneratedDocument document) {
		return new GeneratedDocumentResponse(
				document.getId(),
				document.getGenerationRequest().getId(),
				document.getTemplateVersion().getId(),
				document.getContentType(),
				document.getChecksum(),
				document.getStorageReference(),
				document.getCreatedAt());
	}
}
