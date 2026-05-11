package com.jadelomeiri.documentgenerator.homepage.api;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.jadelomeiri.documentgenerator.artist.Artist;
import com.jadelomeiri.documentgenerator.artist.api.ArtistController;
import com.jadelomeiri.documentgenerator.artist.api.ArtistResponse;
import com.jadelomeiri.documentgenerator.common.api.LinkResponse;
import com.jadelomeiri.documentgenerator.homepage.ArtistOfTheDayService;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/homepage")
public class HomepageController {

	private final ArtistOfTheDayService artistOfTheDayService;

	public HomepageController(ArtistOfTheDayService artistOfTheDayService) {
		this.artistOfTheDayService = artistOfTheDayService;
	}

	@GetMapping("/artist-of-the-day")
	public ArtistResponse getArtistOfTheDay() {
		return toArtistResponse(artistOfTheDayService.getArtistOfTheDay());
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
						"artistOfTheDay", new LinkResponse(artistOfTheDayUri()),
						"aliases", new LinkResponse(artistAliasesUri(artistId)),
						"tracks", new LinkResponse(artistTracksUri(artistId))));
	}

	private String artistOfTheDayUri() {
		return linkTo(HomepageController.class)
				.slash("artist-of-the-day")
				.toUri()
				.toString();
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
