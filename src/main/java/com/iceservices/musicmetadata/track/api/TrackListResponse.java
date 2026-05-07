package com.iceservices.musicmetadata.track.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iceservices.musicmetadata.common.api.LinkResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TrackListResponse(
		UUID artistId,
		List<TrackResponse> tracks,
		PageMetadata page,
		@JsonProperty("_links") Map<String, LinkResponse> links) {

	public record PageMetadata(int number, int size, long totalElements, int totalPages) {
	}
}
