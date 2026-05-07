package com.iceservices.musicmetadata.artist.api;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.iceservices.musicmetadata.artist.Artist;
import com.iceservices.musicmetadata.artist.ArtistAlias;
import com.iceservices.musicmetadata.artist.ArtistService;
import com.iceservices.musicmetadata.common.api.LinkResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/artists")
public class ArtistController {

	private final ArtistService artistService;

	public ArtistController(ArtistService artistService) {
		this.artistService = artistService;
	}

	@PostMapping
	public ResponseEntity<ArtistResponse> createArtist(@Valid @RequestBody CreateArtistRequest request) {
		Artist artist = artistService.createArtist(request.primaryName());
		ArtistResponse response = toArtistResponse(artist);
		return ResponseEntity.created(URI.create(response.links().get("self").href())).body(response);
	}

	@GetMapping("/{artistId}")
	public ArtistResponse getArtist(@PathVariable UUID artistId) {
		return toArtistResponse(artistService.getArtist(artistId));
	}

	@PatchMapping("/{artistId}")
	public ArtistResponse updateArtist(@PathVariable UUID artistId, @Valid @RequestBody UpdateArtistRequest request) {
		return toArtistResponse(artistService.updateArtist(artistId, request.primaryName()));
	}

	@PostMapping("/{artistId}/aliases")
	public ResponseEntity<ArtistAliasResponse> addAlias(
			@PathVariable UUID artistId,
			@Valid @RequestBody CreateArtistAliasRequest request) {
		ArtistAlias alias = artistService.addAlias(artistId, request.alias());
		ArtistAliasResponse response = toAliasResponse(alias, artistId);
		return ResponseEntity.created(URI.create(artistAliasesUri(artistId))).body(response);
	}

	@GetMapping("/{artistId}/aliases")
	public ArtistAliasListResponse listAliases(@PathVariable UUID artistId) {
		List<ArtistAliasResponse> aliases = artistService.listAliases(artistId).stream()
				.map(alias -> toAliasResponse(alias, artistId))
				.toList();
		return new ArtistAliasListResponse(artistId, aliases);
	}

	private ArtistResponse toArtistResponse(Artist artist) {
		UUID artistId = artist.getId();
		return new ArtistResponse(
				artistId,
				artist.getPrimaryName(),
				artist.getCreatedAt(),
				artist.getUpdatedAt(),
				Map.of(
						"self", new LinkResponse(artistUri(artistId)),
						"aliases", new LinkResponse(artistAliasesUri(artistId)),
						"tracks", new LinkResponse(artistTracksUri(artistId))));
	}

	private ArtistAliasResponse toAliasResponse(ArtistAlias alias, UUID artistId) {
		return new ArtistAliasResponse(
				alias.getId(),
				artistId,
				alias.getAliasName(),
				alias.getCreatedAt(),
				alias.getUpdatedAt());
	}

	private String artistUri(UUID artistId) {
		return linkTo(ArtistController.class)
				.slash(artistId)
				.toUri()
				.toString();
	}

	private String artistAliasesUri(UUID artistId) {
		return linkTo(ArtistController.class)
				.slash(artistId)
				.slash("aliases")
				.toUri()
				.toString();
	}

	private String artistTracksUri(UUID artistId) {
		return linkTo(ArtistController.class)
				.slash(artistId)
				.slash("tracks")
				.toUri()
				.toString();
	}
}
