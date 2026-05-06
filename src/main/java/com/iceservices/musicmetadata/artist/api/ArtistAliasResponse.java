package com.iceservices.musicmetadata.artist.api;

import java.time.Instant;
import java.util.UUID;

public record ArtistAliasResponse(
		UUID id,
		UUID artistId,
		String alias,
		Instant createdAt,
		Instant updatedAt) {
}
