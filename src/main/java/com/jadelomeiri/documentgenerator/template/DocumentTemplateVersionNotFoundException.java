package com.jadelomeiri.documentgenerator.template;

import java.util.UUID;

public class DocumentTemplateVersionNotFoundException extends RuntimeException {

	private final UUID templateVersionId;

	public DocumentTemplateVersionNotFoundException(UUID templateVersionId) {
		super("Document template version not found: " + templateVersionId);
		this.templateVersionId = templateVersionId;
	}

	public UUID getTemplateVersionId() {
		return templateVersionId;
	}
}
