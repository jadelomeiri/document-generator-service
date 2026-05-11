package com.jadelomeiri.documentgenerator.generation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateGenerationRequest(
		@NotNull(message = "templateVersionId must not be null")
		UUID templateVersionId,
		@NotBlank(message = "customerReference must not be blank")
		String customerReference,
		@NotBlank(message = "requestedBy must not be blank")
		String requestedBy,
		@NotBlank(message = "inputPayloadJson must not be blank")
		String inputPayloadJson) {
}
