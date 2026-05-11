package com.jadelomeiri.documentgenerator.template;

import java.util.UUID;

public class DocumentTemplateNotFoundException extends RuntimeException {

	private final UUID templateId;

	public DocumentTemplateNotFoundException(UUID templateId) {
		super("Document template not found: " + templateId);
		this.templateId = templateId;
	}

	public UUID getTemplateId() {
		return templateId;
	}
}
