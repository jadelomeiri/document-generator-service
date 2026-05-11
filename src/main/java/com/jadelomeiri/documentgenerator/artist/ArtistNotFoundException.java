package com.jadelomeiri.documentgenerator.artist;

import java.util.UUID;

public class ArtistNotFoundException extends RuntimeException {

	private final UUID artistId;

	public ArtistNotFoundException(UUID artistId) {
		super("Artist not found: " + artistId);
		this.artistId = artistId;
	}

	public UUID getArtistId() {
		return artistId;
	}
}
