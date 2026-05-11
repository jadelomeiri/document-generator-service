package com.jadelomeiri.documentgenerator.generation;

import com.jadelomeiri.documentgenerator.document.GeneratedDocument;

public record GenerationResult(
		DocumentGenerationRequest request,
		GeneratedDocument generatedDocument) {
}
