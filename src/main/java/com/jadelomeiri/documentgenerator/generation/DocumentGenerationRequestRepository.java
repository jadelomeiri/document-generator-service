package com.jadelomeiri.documentgenerator.generation;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentGenerationRequestRepository extends JpaRepository<DocumentGenerationRequest, UUID> {
}
