package com.jadelomeiri.documentgenerator.track;

import com.jadelomeiri.documentgenerator.artist.Artist;
import com.jadelomeiri.documentgenerator.artist.ArtistNotFoundException;
import com.jadelomeiri.documentgenerator.artist.ArtistRepository;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TrackService {

	private final ArtistRepository artistRepository;
	private final TrackRepository trackRepository;

	public TrackService(ArtistRepository artistRepository, TrackRepository trackRepository) {
		this.artistRepository = artistRepository;
		this.trackRepository = trackRepository;
	}

	@Transactional
	public Track addTrack(UUID artistId, String title, String genre, int lengthSeconds, String isrc) {
		Artist artist = artistRepository.findById(artistId)
				.orElseThrow(() -> new ArtistNotFoundException(artistId));

		String normalisedIsrc = normaliseIsrc(isrc);
		if (normalisedIsrc != null && trackRepository.existsByIsrc(normalisedIsrc)) {
			throw new DuplicateTrackIsrcException(normalisedIsrc);
		}

		try {
			return trackRepository.saveAndFlush(new Track(
					artist,
					title.trim(),
					normaliseOptionalText(genre),
					lengthSeconds,
					normalisedIsrc));
		} catch (DataIntegrityViolationException ex) {
			if (normalisedIsrc != null) {
				throw new DuplicateTrackIsrcException(normalisedIsrc);
			}
			throw ex;
		}
	}

	public Page<Track> listTracks(UUID artistId, int page, int size) {
		if (!artistRepository.existsById(artistId)) {
			throw new ArtistNotFoundException(artistId);
		}
		Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending().and(Sort.by("id").ascending()));
		return trackRepository.findByArtistId(artistId, pageable);
	}

	private String normaliseOptionalText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isBlank() ? null : trimmed;
	}

	private String normaliseIsrc(String isrc) {
		String normalised = normaliseOptionalText(isrc);
		return normalised == null ? null : normalised.toUpperCase(Locale.ROOT);
	}
}
