package com.iceservices.musicmetadata.artist.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateArtistRequest(
		@NotBlank(message = "primaryName must not be blank")
		@Size(max = 255, message = "primaryName must be at most 255 characters")
		String primaryName) {
}
