package com.jadelomeiri.documentgenerator.template;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TemplateService {

	private final DocumentTemplateRepository templateRepository;
	private final DocumentTemplateVersionRepository templateVersionRepository;

	public TemplateService(
			DocumentTemplateRepository templateRepository,
			DocumentTemplateVersionRepository templateVersionRepository) {
		this.templateRepository = templateRepository;
		this.templateVersionRepository = templateVersionRepository;
	}

	public List<DocumentTemplate> listTemplates() {
		return templateRepository.findAllByOrderByNameAsc();
	}

	public DocumentTemplate getTemplate(UUID templateId) {
		return templateRepository.findById(templateId)
				.orElseThrow(() -> new DocumentTemplateNotFoundException(templateId));
	}

	public List<DocumentTemplateVersion> listVersions(UUID templateId) {
		if (!templateRepository.existsById(templateId)) {
			throw new DocumentTemplateNotFoundException(templateId);
		}
		return templateVersionRepository.findByTemplateIdOrderByVersionNumberAsc(templateId);
	}

	public DocumentTemplateVersion getTemplateVersion(UUID templateVersionId) {
		return templateVersionRepository.findById(templateVersionId)
				.orElseThrow(() -> new DocumentTemplateVersionNotFoundException(templateVersionId));
	}
}
