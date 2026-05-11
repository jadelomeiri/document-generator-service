package com.jadelomeiri.documentgenerator.template;

import java.util.List;
import java.util.UUID;

public record DocumentTemplateVersionListResponse(UUID templateId, List<DocumentTemplateVersionResponse> versions) {
}
