package com.iceservices.musicmetadata.track.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iceservices.musicmetadata.artist.api.LinkResponse;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TrackResponse(
		UUID id,
		UUID artistId,
		String title,
		String genre,
		int lengthSeconds,
		String isrc,
		Instant createdAt,
		Instant updatedAt,
		@JsonProperty("_links") Map<String, LinkResponse> links) {
}
