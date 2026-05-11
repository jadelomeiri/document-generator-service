package com.jadelomeiri.documentgenerator.generation;

import com.jadelomeiri.documentgenerator.audit.AuditService;
import com.jadelomeiri.documentgenerator.document.GeneratedDocument;
import com.jadelomeiri.documentgenerator.document.GeneratedDocumentDraft;
import com.jadelomeiri.documentgenerator.document.GeneratedDocumentService;
import com.jadelomeiri.documentgenerator.template.DocumentTemplateVersion;
import com.jadelomeiri.documentgenerator.template.TemplateService;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GenerationRequestService {

	private final DocumentGenerationRequestRepository generationRequestRepository;
	private final TemplateService templateService;
	private final DocumentGenerationService documentGenerationService;
	private final GeneratedDocumentService generatedDocumentService;
	private final AuditService auditService;
	private final Clock clock;

	public GenerationRequestService(
			DocumentGenerationRequestRepository generationRequestRepository,
			TemplateService templateService,
			DocumentGenerationService documentGenerationService,
			GeneratedDocumentService generatedDocumentService,
			AuditService auditService,
			Clock clock) {
		this.generationRequestRepository = generationRequestRepository;
		this.templateService = templateService;
		this.documentGenerationService = documentGenerationService;
		this.generatedDocumentService = generatedDocumentService;
		this.auditService = auditService;
		this.clock = clock;
	}

	@Transactional
	public GenerationResult createGenerationRequest(
			UUID templateVersionId,
			String customerReference,
			String requestedBy,
			String inputPayloadJson) {
		DocumentTemplateVersion templateVersion = templateService.getTemplateVersion(templateVersionId);
		DocumentGenerationRequest request = generationRequestRepository.saveAndFlush(new DocumentGenerationRequest(
				templateVersion,
				customerReference.trim(),
				requestedBy.trim(),
				inputPayloadJson.trim()));
		auditService.record(
				"GENERATION_REQUEST_CREATED",
				AuditService.GENERATION_REQUEST_TARGET,
				request.getId(),
				request.getRequestedBy(),
				detailsJson(request));

		try {
			request.markValidated();
			request.markGenerating();
			GeneratedDocumentDraft generatedDocumentDraft = documentGenerationService.generate(request);
			GeneratedDocument generatedDocument = generatedDocumentService.create(request, generatedDocumentDraft);
			request.markCompleted(Instant.now(clock));
			auditService.record(
					"GENERATION_COMPLETED",
					AuditService.GENERATION_REQUEST_TARGET,
					request.getId(),
					request.getRequestedBy(),
					"{\"generatedDocumentId\":\"" + generatedDocument.getId() + "\"}");
			return new GenerationResult(request, generatedDocument);
		} catch (RuntimeException ex) {
			request.markFailed(ex.getMessage());
			auditService.record(
					"GENERATION_FAILED",
					AuditService.GENERATION_REQUEST_TARGET,
					request.getId(),
					request.getRequestedBy(),
					"{\"reason\":\"" + safeJsonValue(ex.getMessage()) + "\"}");
			return new GenerationResult(request, null);
		}
	}

	public DocumentGenerationRequest getGenerationRequest(UUID requestId) {
		return generationRequestRepository.findById(requestId)
				.orElseThrow(() -> new GenerationRequestNotFoundException(requestId));
	}

	private String detailsJson(DocumentGenerationRequest request) {
		return "{\"templateVersionId\":\"" + request.getTemplateVersion().getId() + "\"}";
	}

	private String safeJsonValue(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
