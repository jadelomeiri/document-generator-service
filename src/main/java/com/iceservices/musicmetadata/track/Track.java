package com.iceservices.musicmetadata.track;

import com.iceservices.musicmetadata.artist.Artist;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "tracks")
public class Track {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "artist_id", nullable = false, foreignKey = @ForeignKey(name = "tracks_artist_fk"))
	private Artist artist;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(length = 100)
	private String genre;

	@Column(name = "length_seconds", nullable = false)
	private int lengthSeconds;

	@Column(length = 12)
	private String isrc;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Track() {
	}

	public Track(Artist artist, String title, String genre, int lengthSeconds, String isrc) {
		this.artist = artist;
		this.title = title;
		this.genre = genre;
		this.lengthSeconds = lengthSeconds;
		this.isrc = isrc;
	}

	public UUID getId() {
		return id;
	}

	public Artist getArtist() {
		return artist;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public int getLengthSeconds() {
		return lengthSeconds;
	}

	public void setLengthSeconds(int lengthSeconds) {
		this.lengthSeconds = lengthSeconds;
	}

	public String getIsrc() {
		return isrc;
	}

	public void setIsrc(String isrc) {
		this.isrc = isrc;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}
