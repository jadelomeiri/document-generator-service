package com.jadelomeiri.documentgenerator.track.api;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import com.jadelomeiri.documentgenerator.artist.api.ArtistController;
import com.jadelomeiri.documentgenerator.common.api.LinkResponse;
import com.jadelomeiri.documentgenerator.track.Track;
import com.jadelomeiri.documentgenerator.track.TrackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/artists/{artistId}/tracks")
public class TrackController {

	private final TrackService trackService;

	public TrackController(TrackService trackService) {
		this.trackService = trackService;
	}

	@PostMapping
	public ResponseEntity<TrackResponse> addTrack(
			@PathVariable UUID artistId,
			@Valid @RequestBody CreateTrackRequest request) {
		Track track = trackService.addTrack(
				artistId,
				request.title(),
				request.genre(),
				request.lengthSeconds(),
				request.isrc());
		TrackResponse response = toTrackResponse(track, artistId);
		return ResponseEntity.created(URI.create(tracksUri(artistId))).body(response);
	}

	@GetMapping
	public TrackListResponse listTracks(
			@PathVariable UUID artistId,
			@RequestParam(defaultValue = "0") @Min(value = 0, message = "page must not be negative") int page,
			@RequestParam(defaultValue = "50") @Min(value = 1, message = "size must be positive")
					@Max(value = 100, message = "size must be at most 100") int size) {
		Page<Track> tracks = trackService.listTracks(artistId, page, size);
		return new TrackListResponse(
				artistId,
				tracks.getContent().stream().map(track -> toTrackResponse(track, artistId)).toList(),
				new TrackListResponse.PageMetadata(
						tracks.getNumber(),
						tracks.getSize(),
						tracks.getTotalElements(),
						tracks.getTotalPages()),
				listLinks(artistId, tracks));
	}

	private TrackResponse toTrackResponse(Track track, UUID artistId) {
		return new TrackResponse(
				track.getId(),
				artistId,
				track.getTitle(),
				track.getGenre(),
				track.getLengthSeconds(),
				track.getIsrc(),
				track.getCreatedAt(),
				track.getUpdatedAt(),
				Map.of(
						"artist", new LinkResponse(artistUri(artistId)),
						"tracks", new LinkResponse(tracksUri(artistId))));
	}

	private Map<String, LinkResponse> listLinks(UUID artistId, Page<Track> tracks) {
		Map<String, LinkResponse> links = new LinkedHashMap<>();
		links.put("self", new LinkResponse(tracksUri(artistId, tracks.getNumber(), tracks.getSize())));
		links.put("artist", new LinkResponse(artistUri(artistId)));
		if (tracks.hasPrevious()) {
			links.put("previous", new LinkResponse(tracksUri(artistId, tracks.getNumber() - 1, tracks.getSize())));
		}
		if (tracks.hasNext()) {
			links.put("next", new LinkResponse(tracksUri(artistId, tracks.getNumber() + 1, tracks.getSize())));
		}
		return links;
	}

	private String artistUri(UUID artistId) {
		return linkTo(ArtistController.class).slash(artistId).toUri().toString();
	}

	private String tracksUri(UUID artistId) {
		return linkTo(ArtistController.class).slash(artistId).slash("tracks").toUri().toString();
	}

	private String tracksUri(UUID artistId, int page, int size) {
		return tracksUri(artistId) + "?page=" + page + "&size=" + size;
	}
}
