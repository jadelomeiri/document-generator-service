package com.iceservices.musicmetadata.artist.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ArtistResponse(
		UUID id,
		String primaryName,
		Instant createdAt,
		Instant updatedAt,
		@JsonProperty("_links") Map<String, LinkResponse> links) {
}
