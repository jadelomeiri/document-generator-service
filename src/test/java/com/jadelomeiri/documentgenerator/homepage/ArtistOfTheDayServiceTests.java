package com.jadelomeiri.documentgenerator.homepage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jadelomeiri.documentgenerator.artist.Artist;
import com.jadelomeiri.documentgenerator.artist.ArtistRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ArtistOfTheDayServiceTests {

	@Mock
	ArtistRepository artistRepository;

	@Test
	void emptyRepositoryCountThrowsArtistOfTheDayNotFoundException() {
		ArtistOfTheDayService service = serviceAt("2024-01-01T12:00:00Z");
		when(artistRepository.count()).thenReturn(0L);

		assertThatThrownBy(service::getArtistOfTheDay)
				.isInstanceOf(ArtistOfTheDayNotFoundException.class);
	}

	@Test
	void epochDateSelectsPageIndexZero() {
		Artist selectedArtist = new Artist("Aphex Twin");
		ArtistOfTheDayService service = serviceAt("2024-01-01T12:00:00Z");
		when(artistRepository.count()).thenReturn(3L);
		when(artistRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(selectedArtist)));

		Artist result = service.getArtistOfTheDay();

		assertThat(result).isSameAs(selectedArtist);
		assertPageRequest(0);
	}

	@Test
	void moduloWrapAroundWithThreeArtistsSelectsPageIndexZero() {
		Artist selectedArtist = new Artist("Björk");
		ArtistOfTheDayService service = serviceAt("2024-01-04T12:00:00Z");
		when(artistRepository.count()).thenReturn(3L);
		when(artistRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(selectedArtist)));

		Artist result = service.getArtistOfTheDay();

		assertThat(result).isSameAs(selectedArtist);
		assertPageRequest(0);
	}

	@Test
	void dateBeforeEpochUsesFloorModAndSelectsNonNegativeValidIndex() {
		Artist selectedArtist = new Artist("Daft Punk");
		ArtistOfTheDayService service = serviceAt("2023-12-31T12:00:00Z");
		when(artistRepository.count()).thenReturn(3L);
		when(artistRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(selectedArtist)));

		Artist result = service.getArtistOfTheDay();

		assertThat(result).isSameAs(selectedArtist);
		assertPageRequest(2);
	}

	@Test
	void repositoryQueryUsesOneRowPageRequestAndStableSort() {
		Artist selectedArtist = new Artist("Massive Attack");
		ArtistOfTheDayService service = serviceAt("2024-01-02T12:00:00Z");
		when(artistRepository.count()).thenReturn(2L);
		when(artistRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(java.util.List.of(selectedArtist)));

		service.getArtistOfTheDay();

		Pageable pageable = capturedPageable();
		assertThat(pageable.getPageNumber()).isEqualTo(1);
		assertThat(pageable.getPageSize()).isEqualTo(1);
		assertThat(pageable.getSort()).containsExactly(
				Sort.Order.asc("createdAt"),
				Sort.Order.asc("id"));
	}

	private ArtistOfTheDayService serviceAt(String instant) {
		Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
		return new ArtistOfTheDayService(artistRepository, clock);
	}

	private void assertPageRequest(int expectedPageNumber) {
		Pageable pageable = capturedPageable();
		assertThat(pageable.getPageNumber()).isEqualTo(expectedPageNumber);
		assertThat(pageable.getPageSize()).isEqualTo(1);
		assertThat(pageable.getSort()).containsExactly(
				Sort.Order.asc("createdAt"),
				Sort.Order.asc("id"));
	}

	private Pageable capturedPageable() {
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(artistRepository).findAll(pageableCaptor.capture());
		return pageableCaptor.getValue();
	}
}
