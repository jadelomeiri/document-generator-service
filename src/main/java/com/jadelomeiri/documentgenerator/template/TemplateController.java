package com.jadelomeiri.documentgenerator.template;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.jadelomeiri.documentgenerator.common.api.LinkResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

	private final TemplateService templateService;

	public TemplateController(TemplateService templateService) {
		this.templateService = templateService;
	}

	@GetMapping
	public DocumentTemplateListResponse listTemplates() {
		List<DocumentTemplateResponse> templates = templateService.listTemplates().stream()
				.map(this::toTemplateResponse)
				.toList();
		return new DocumentTemplateListResponse(templates);
	}

	@GetMapping("/{templateId}")
	public DocumentTemplateResponse getTemplate(@PathVariable UUID templateId) {
		return toTemplateResponse(templateService.getTemplate(templateId));
	}

	@GetMapping("/{templateId}/versions")
	public DocumentTemplateVersionListResponse listVersions(@PathVariable UUID templateId) {
		List<DocumentTemplateVersionResponse> versions = templateService.listVersions(templateId).stream()
				.map(this::toVersionResponse)
				.toList();
		return new DocumentTemplateVersionListResponse(templateId, versions);
	}

	private DocumentTemplateResponse toTemplateResponse(DocumentTemplate template) {
		UUID templateId = template.getId();
		return new DocumentTemplateResponse(
				templateId,
				template.getName(),
				template.getDescription(),
				template.isActive(),
				template.getCreatedAt(),
				template.getUpdatedAt(),
				Map.of(
						"self", new LinkResponse(templateUri(templateId)),
						"versions", new LinkResponse(templateVersionsUri(templateId))));
	}

	private DocumentTemplateVersionResponse toVersionResponse(DocumentTemplateVersion version) {
		return new DocumentTemplateVersionResponse(
				version.getId(),
				version.getTemplate().getId(),
				version.getVersionNumber(),
				version.getFormat(),
				version.getTemplateLocation(),
				version.getStatus(),
				version.getCreatedAt(),
				version.getActivatedAt());
	}

	private String templateUri(UUID templateId) {
		return linkTo(TemplateController.class).slash(templateId).toUri().toString();
	}

	private String templateVersionsUri(UUID templateId) {
		return linkTo(TemplateController.class).slash(templateId).slash("versions").toUri().toString();
	}
}
