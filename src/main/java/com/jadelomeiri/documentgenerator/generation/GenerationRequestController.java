package com.jadelomeiri.documentgenerator.generation;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.jadelomeiri.documentgenerator.audit.AuditEvent;
import com.jadelomeiri.documentgenerator.audit.AuditEventListResponse;
import com.jadelomeiri.documentgenerator.audit.AuditEventResponse;
import com.jadelomeiri.documentgenerator.audit.AuditService;
import com.jadelomeiri.documentgenerator.common.api.LinkResponse;
import com.jadelomeiri.documentgenerator.document.GeneratedDocument;
import com.jadelomeiri.documentgenerator.document.GeneratedDocumentController;
import com.jadelomeiri.documentgenerator.document.GeneratedDocumentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/generation-requests")
public class GenerationRequestController {

	private final GenerationRequestService generationRequestService;
	private final GeneratedDocumentService generatedDocumentService;
	private final AuditService auditService;

	public GenerationRequestController(
			GenerationRequestService generationRequestService,
			GeneratedDocumentService generatedDocumentService,
			AuditService auditService) {
		this.generationRequestService = generationRequestService;
		this.generatedDocumentService = generatedDocumentService;
		this.auditService = auditService;
	}

	@PostMapping
	public ResponseEntity<GenerationRequestResponse> createGenerationRequest(
			@Valid @RequestBody CreateGenerationRequest request) {
		GenerationResult result = generationRequestService.createGenerationRequest(
				request.templateVersionId(),
				request.customerReference(),
				request.requestedBy(),
				request.inputPayloadJson());
		GenerationRequestResponse response = toResponse(result.request(), result.generatedDocument());
		return ResponseEntity.created(URI.create(response.links().get("self").href())).body(response);
	}

	@GetMapping("/{requestId}")
	public GenerationRequestResponse getGenerationRequest(@PathVariable UUID requestId) {
		DocumentGenerationRequest request = generationRequestService.getGenerationRequest(requestId);
		GeneratedDocument generatedDocument = request.getStatus() == GenerationRequestStatus.COMPLETED
				? generatedDocumentService.getByGenerationRequest(requestId) : null;
		return toResponse(request, generatedDocument);
	}

	@GetMapping("/{requestId}/audit-events")
	public AuditEventListResponse listAuditEvents(@PathVariable UUID requestId) {
		generationRequestService.getGenerationRequest(requestId);
		List<AuditEventResponse> events = auditService.eventsForGenerationRequest(requestId).stream()
				.map(this::toAuditEventResponse)
				.toList();
		return new AuditEventListResponse(events);
	}

	private GenerationRequestResponse toResponse(DocumentGenerationRequest request, GeneratedDocument generatedDocument) {
		UUID requestId = request.getId();
		return new GenerationRequestResponse(
				requestId,
				request.getTemplateVersion().getId(),
				request.getCustomerReference(),
				request.getRequestedBy(),
				request.getStatus(),
				request.getInputPayloadJson(),
				request.getFailureReason(),
				request.getCreatedAt(),
				request.getUpdatedAt(),
				request.getCompletedAt(),
				generatedDocument == null ? null : GeneratedDocumentController.toResponse(generatedDocument),
				Map.of(
						"self", new LinkResponse(requestUri(requestId)),
						"auditEvents", new LinkResponse(auditEventsUri(requestId))));
	}

	private AuditEventResponse toAuditEventResponse(AuditEvent auditEvent) {
		return new AuditEventResponse(
				auditEvent.getId(),
				auditEvent.getEventType(),
				auditEvent.getTargetType(),
				auditEvent.getTargetId(),
				auditEvent.getActorReference(),
				auditEvent.getDetailsJson(),
				auditEvent.getOccurredAt());
	}

	private String requestUri(UUID requestId) {
		return linkTo(GenerationRequestController.class).slash(requestId).toUri().toString();
	}

	private String auditEventsUri(UUID requestId) {
		return linkTo(GenerationRequestController.class).slash(requestId).slash("audit-events").toUri().toString();
	}
}
