package com.jadelomeiri.documentgenerator.track;

public class DuplicateTrackIsrcException extends RuntimeException {

	private final String isrc;

	public DuplicateTrackIsrcException(String isrc) {
		super("Track with ISRC already exists: " + isrc);
		this.isrc = isrc;
	}

	public String getIsrc() {
		return isrc;
	}
}
