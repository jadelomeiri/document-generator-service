package com.jadelomeiri.documentgenerator.document;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedDocumentRepository extends JpaRepository<GeneratedDocument, UUID> {

	Optional<GeneratedDocument> findByGenerationRequestId(UUID generationRequestId);
}
