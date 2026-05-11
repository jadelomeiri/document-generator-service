package com.jadelomeiri.documentgenerator.template;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, UUID> {

	List<DocumentTemplate> findAllByOrderByNameAsc();
}
