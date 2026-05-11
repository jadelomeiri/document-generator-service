package com.jadelomeiri.documentgenerator.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuditService {

	public static final String GENERATION_REQUEST_TARGET = "DOCUMENT_GENERATION_REQUEST";

	private final AuditEventRepository auditEventRepository;

	public AuditService(AuditEventRepository auditEventRepository) {
		this.auditEventRepository = auditEventRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public AuditEvent record(String eventType, String targetType, UUID targetId, String actorReference, String detailsJson) {
		return auditEventRepository.save(new AuditEvent(eventType, targetType, targetId, actorReference, detailsJson));
	}

	public List<AuditEvent> eventsForGenerationRequest(UUID requestId) {
		return auditEventRepository.findByTargetTypeAndTargetIdOrderByOccurredAtAsc(GENERATION_REQUEST_TARGET, requestId);
	}
}
