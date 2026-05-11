package com.jadelomeiri.documentgenerator.document;

import com.jadelomeiri.documentgenerator.generation.DocumentGenerationRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GeneratedDocumentService {

	private final GeneratedDocumentRepository generatedDocumentRepository;

	public GeneratedDocumentService(GeneratedDocumentRepository generatedDocumentRepository) {
		this.generatedDocumentRepository = generatedDocumentRepository;
	}

	@Transactional
	public GeneratedDocument create(DocumentGenerationRequest request, GeneratedDocumentDraft draft) {
		GeneratedDocument document = new GeneratedDocument(
				request,
				request.getTemplateVersion(),
				draft.contentType(),
				draft.checksum(),
				draft.storageReference());
		return generatedDocumentRepository.save(document);
	}

	public GeneratedDocument getGeneratedDocument(UUID documentId) {
		return generatedDocumentRepository.findById(documentId)
				.orElseThrow(() -> new GeneratedDocumentNotFoundException(documentId));
	}

	public GeneratedDocument getByGenerationRequest(UUID requestId) {
		return generatedDocumentRepository.findByGenerationRequestId(requestId)
				.orElseThrow(() -> new GeneratedDocumentNotFoundException(requestId));
	}
}
