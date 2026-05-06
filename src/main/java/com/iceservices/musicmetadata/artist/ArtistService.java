package com.iceservices.musicmetadata.artist;

import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ArtistService {

	private final ArtistRepository artistRepository;
	private final ArtistAliasRepository artistAliasRepository;

	public ArtistService(ArtistRepository artistRepository, ArtistAliasRepository artistAliasRepository) {
		this.artistRepository = artistRepository;
		this.artistAliasRepository = artistAliasRepository;
	}

	@Transactional
	public Artist createArtist(String primaryName) {
		return artistRepository.save(new Artist(normaliseName(primaryName)));
	}

	public Artist getArtist(UUID artistId) {
		return artistRepository.findById(artistId)
				.orElseThrow(() -> new ArtistNotFoundException(artistId));
	}

	@Transactional
	public Artist updateArtist(UUID artistId, String primaryName) {
		Artist artist = getArtist(artistId);
		artist.setPrimaryName(normaliseName(primaryName));
		return artist;
	}

	@Transactional
	public ArtistAlias addAlias(UUID artistId, String alias) {
		Artist artist = getArtist(artistId);
		String normalisedAlias = normaliseName(alias);

		if (artistAliasRepository.existsByArtistIdAndAliasNameIgnoreCase(artistId, normalisedAlias)) {
			throw new DuplicateArtistAliasException(artistId, normalisedAlias);
		}

		try {
			return artistAliasRepository.saveAndFlush(new ArtistAlias(artist, normalisedAlias));
		}
		catch (DataIntegrityViolationException ex) {
			throw new DuplicateArtistAliasException(artistId, normalisedAlias);
		}
	}

	public List<ArtistAlias> listAliases(UUID artistId) {
		if (!artistRepository.existsById(artistId)) {
			throw new ArtistNotFoundException(artistId);
		}
		return artistAliasRepository.findByArtistIdOrderByAliasNameAsc(artistId);
	}

	private String normaliseName(String value) {
		return value.trim();
	}
}
