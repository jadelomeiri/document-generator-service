package com.iceservices.musicmetadata.artist;

import java.util.UUID;

public class DuplicateArtistAliasException extends RuntimeException {

	private final UUID artistId;
	private final String alias;

	public DuplicateArtistAliasException(UUID artistId, String alias) {
		super("Alias already exists for artist " + artistId + ": " + alias);
		this.artistId = artistId;
		this.alias = alias;
	}

	public UUID getArtistId() {
		return artistId;
	}

	public String getAlias() {
		return alias;
	}
}
