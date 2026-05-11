package com.jadelomeiri.documentgenerator.artist.api;

import java.util.List;
import java.util.UUID;

public record ArtistAliasListResponse(UUID artistId, List<ArtistAliasResponse> aliases) {
}
