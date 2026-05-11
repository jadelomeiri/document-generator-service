package com.jadelomeiri.documentgenerator.track;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jadelomeiri.documentgenerator.artist.Artist;
import com.jadelomeiri.documentgenerator.artist.ArtistNotFoundException;
import com.jadelomeiri.documentgenerator.artist.ArtistRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class TrackServiceTests {

	@Mock
	ArtistRepository artistRepository;

	@Mock
	TrackRepository trackRepository;

	@Test
	void addTrackTrimsTitleAndNonblankGenre() {
		UUID artistId = UUID.randomUUID();
		Artist artist = new Artist("Massive Attack");
		TrackService service = new TrackService(artistRepository, trackRepository);
		when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
		when(trackRepository.saveAndFlush(any(Track.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Track result = service.addTrack(artistId, "  Teardrop  ", "  Trip hop  ", 330, null);

		Track savedTrack = savedTrack();
		assertThat(savedTrack.getArtist()).isSameAs(artist);
		assertThat(savedTrack.getTitle()).isEqualTo("Teardrop");
		assertThat(savedTrack.getGenre()).isEqualTo("Trip hop");
		assertThat(result.getTitle()).isEqualTo("Teardrop");
	}

	@Test
	void blankGenreBecomesNull() {
		UUID artistId = UUID.randomUUID();
		TrackService service = serviceWithArtist(artistId);
		when(trackRepository.saveAndFlush(any(Track.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Track result = service.addTrack(artistId, "Archangel", "   ", 240, null);

		assertThat(savedTrack().getGenre()).isNull();
		assertThat(result.getGenre()).isNull();
	}

	@Test
	void blankIsrcBecomesNullAndDuplicateLookupIsSkipped() {
		UUID artistId = UUID.randomUUID();
		TrackService service = serviceWithArtist(artistId);
		when(trackRepository.saveAndFlush(any(Track.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Track result = service.addTrack(artistId, "Untitled", null, 180, "   ");

		assertThat(savedTrack().getIsrc()).isNull();
		assertThat(result.getIsrc()).isNull();
		verify(trackRepository, never()).existsByIsrc(any(String.class));
	}

	@Test
	void isrcIsTrimmedAndUppercased() {
		UUID artistId = UUID.randomUUID();
		TrackService service = serviceWithArtist(artistId);
		when(trackRepository.existsByIsrc("USVIR0001107")).thenReturn(false);
		when(trackRepository.saveAndFlush(any(Track.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Track result = service.addTrack(artistId, "One More Time", null, 320, " usvir0001107 ");

		verify(trackRepository).existsByIsrc("USVIR0001107");
		assertThat(savedTrack().getIsrc()).isEqualTo("USVIR0001107");
		assertThat(result.getIsrc()).isEqualTo("USVIR0001107");
	}

	@Test
	void existingIsrcThrowsDuplicateTrackIsrcException() {
		UUID artistId = UUID.randomUUID();
		TrackService service = serviceWithArtist(artistId);
		when(trackRepository.existsByIsrc("USWB10003013")).thenReturn(true);

		assertThatThrownBy(() -> service.addTrack(artistId, "Second Track", null, 250, " uswb10003013 "))
				.isInstanceOfSatisfying(DuplicateTrackIsrcException.class,
						exception -> assertThat(exception.getIsrc()).isEqualTo("USWB10003013"));
		verify(trackRepository, never()).saveAndFlush(any(Track.class));
	}

	@Test
	void dataIntegrityViolationDuringSaveWithNonNullIsrcMapsToDuplicateTrackIsrcException() {
		UUID artistId = UUID.randomUUID();
		TrackService service = serviceWithArtist(artistId);
		when(trackRepository.existsByIsrc("USWB10003013")).thenReturn(false);
		when(trackRepository.saveAndFlush(any(Track.class)))
				.thenThrow(new DataIntegrityViolationException("duplicate isrc"));

		assertThatThrownBy(() -> service.addTrack(artistId, "First Track", null, 250, " uswb10003013 "))
				.isInstanceOfSatisfying(DuplicateTrackIsrcException.class,
						exception -> assertThat(exception.getIsrc()).isEqualTo("USWB10003013"));
	}

	@Test
	void dataIntegrityViolationDuringSaveWithNullIsrcIsRethrown() {
		UUID artistId = UUID.randomUUID();
		DataIntegrityViolationException dataIntegrityViolation = new DataIntegrityViolationException("constraint failure");
		TrackService service = serviceWithArtist(artistId);
		when(trackRepository.saveAndFlush(any(Track.class))).thenThrow(dataIntegrityViolation);

		assertThatThrownBy(() -> service.addTrack(artistId, "Untitled", null, 180, "   "))
				.isSameAs(dataIntegrityViolation);
		verify(trackRepository, never()).existsByIsrc(any(String.class));
	}

	@Test
	void missingArtistThrowsArtistNotFoundException() {
		UUID artistId = UUID.randomUUID();
		TrackService service = new TrackService(artistRepository, trackRepository);
		when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.addTrack(artistId, "Missing Track", null, 180, null))
				.isInstanceOfSatisfying(ArtistNotFoundException.class,
						exception -> assertThat(exception.getArtistId()).isEqualTo(artistId));
		verify(trackRepository, never()).saveAndFlush(any(Track.class));
	}

	private TrackService serviceWithArtist(UUID artistId) {
		when(artistRepository.findById(artistId)).thenReturn(Optional.of(new Artist("Radiohead")));
		return new TrackService(artistRepository, trackRepository);
	}

	private Track savedTrack() {
		ArgumentCaptor<Track> trackCaptor = ArgumentCaptor.forClass(Track.class);
		verify(trackRepository).saveAndFlush(trackCaptor.capture());
		return trackCaptor.getValue();
	}
}
