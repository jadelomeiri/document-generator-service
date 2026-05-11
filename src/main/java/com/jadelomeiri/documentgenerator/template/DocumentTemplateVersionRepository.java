package com.jadelomeiri.documentgenerator.template;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTemplateVersionRepository extends JpaRepository<DocumentTemplateVersion, UUID> {

	List<DocumentTemplateVersion> findByTemplateIdOrderByVersionNumberAsc(UUID templateId);
}
