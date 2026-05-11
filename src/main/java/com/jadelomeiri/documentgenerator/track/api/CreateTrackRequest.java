package com.jadelomeiri.documentgenerator.track.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTrackRequest(
		@NotBlank(message = "title must not be blank")
		@Size(max = 255, message = "title must be at most 255 characters")
		String title,

		@Size(max = 100, message = "genre must be at most 100 characters")
		String genre,

		@NotNull(message = "lengthSeconds is required")
		@Positive(message = "lengthSeconds must be positive")
		Integer lengthSeconds,

		@Pattern(
				regexp = "^\\s*[A-Za-z]{2}[A-Za-z0-9]{3}[0-9]{7}\\s*$",
				message = "isrc must be a valid 12-character ISRC")
		String isrc) {
}
