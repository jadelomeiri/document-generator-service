package com.jadelomeiri.documentgenerator.artist.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateArtistAliasRequest(
		@NotBlank(message = "alias must not be blank")
		@Size(max = 255, message = "alias must be at most 255 characters")
		String alias) {
}
