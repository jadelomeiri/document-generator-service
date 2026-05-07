package com.iceservices.musicmetadata.artist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTests {

	@Mock
	ArtistRepository artistRepository;

	@Mock
	ArtistAliasRepository artistAliasRepository;

	@Test
	void createArtistTrimsPrimaryNameBeforeSaving() {
		ArtistService service = new ArtistService(artistRepository, artistAliasRepository);
		when(artistRepository.save(any(Artist.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Artist result = service.createArtist("  Aphex Twin  ");

		ArgumentCaptor<Artist> artistCaptor = ArgumentCaptor.forClass(Artist.class);
		verify(artistRepository).save(artistCaptor.capture());
		assertThat(artistCaptor.getValue().getPrimaryName()).isEqualTo("Aphex Twin");
		assertThat(result.getPrimaryName()).isEqualTo("Aphex Twin");
	}

	@Test
	void updateArtistTrimsPrimaryName() {
		UUID artistId = UUID.randomUUID();
		Artist artist = new Artist("Old Name");
		ArtistService service = new ArtistService(artistRepository, artistAliasRepository);
		when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

		Artist result = service.updateArtist(artistId, "  New Name  ");

		assertThat(result).isSameAs(artist);
		assertThat(artist.getPrimaryName()).isEqualTo("New Name");
	}

	@Test
	void getArtistMissingIdThrowsArtistNotFoundException() {
		UUID artistId = UUID.randomUUID();
		ArtistService service = new ArtistService(artistRepository, artistAliasRepository);
		when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getArtist(artistId))
				.isInstanceOfSatisfying(ArtistNotFoundException.class,
						exception -> assertThat(exception.getArtistId()).isEqualTo(artistId));
	}

	@Test
	void addAliasTrimsAliasBeforeDuplicateCheckAndSave() {
		UUID artistId = UUID.randomUUID();
		Artist artist = new Artist("Aphex Twin");
		ArtistService service = new ArtistService(artistRepository, artistAliasRepository);
		when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
		when(artistAliasRepository.existsByArtistIdAndAliasNameIgnoreCase(artistId, "AFX")).thenReturn(false);
		when(artistAliasRepository.saveAndFlush(any(ArtistAlias.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ArtistAlias result = service.addAlias(artistId, "  AFX  ");

		verify(artistAliasRepository).existsByArtistIdAndAliasNameIgnoreCase(artistId, "AFX");
		ArgumentCaptor<ArtistAlias> aliasCaptor = ArgumentCaptor.forClass(ArtistAlias.class);
		verify(artistAliasRepository).saveAndFlush(aliasCaptor.capture());
		assertThat(aliasCaptor.getValue().getArtist()).isSameAs(artist);
		assertThat(aliasCaptor.getValue().getAliasName()).isEqualTo("AFX");
		assertThat(result.getAliasName()).isEqualTo("AFX");
	}

	@Test
	void duplicateAliasPreCheckThrowsDuplicateArtistAliasException() {
		UUID artistId = UUID.randomUUID();
		ArtistService service = new ArtistService(artistRepository, artistAliasRepository);
		when(artistRepository.findById(artistId)).thenReturn(Optional.of(new Artist("Aphex Twin")));
		when(artistAliasRepository.existsByArtistIdAndAliasNameIgnoreCase(artistId, "AFX")).thenReturn(true);

		assertThatThrownBy(() -> service.addAlias(artistId, "  AFX  "))
				.isInstanceOfSatisfying(DuplicateArtistAliasException.class, exception -> {
					assertThat(exception.getArtistId()).isEqualTo(artistId);
					assertThat(exception.getAlias()).isEqualTo("AFX");
				});
		verify(artistAliasRepository, never()).saveAndFlush(any(ArtistAlias.class));
	}

	@Test
	void dataIntegrityViolationDuringAliasSaveMapsToDuplicateArtistAliasException() {
		UUID artistId = UUID.randomUUID();
		ArtistService service = new ArtistService(artistRepository, artistAliasRepository);
		when(artistRepository.findById(artistId)).thenReturn(Optional.of(new Artist("Aphex Twin")));
		when(artistAliasRepository.existsByArtistIdAndAliasNameIgnoreCase(artistId, "AFX")).thenReturn(false);
		when(artistAliasRepository.saveAndFlush(any(ArtistAlias.class)))
				.thenThrow(new DataIntegrityViolationException("duplicate alias"));

		assertThatThrownBy(() -> service.addAlias(artistId, "  AFX  "))
				.isInstanceOfSatisfying(DuplicateArtistAliasException.class, exception -> {
					assertThat(exception.getArtistId()).isEqualTo(artistId);
					assertThat(exception.getAlias()).isEqualTo("AFX");
				});
	}
}
