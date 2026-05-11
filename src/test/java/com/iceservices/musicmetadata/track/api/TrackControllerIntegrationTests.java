package com.iceservices.musicmetadata.track.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iceservices.musicmetadata.TestcontainersConfiguration;
import com.iceservices.musicmetadata.artist.ArtistAliasRepository;
import com.iceservices.musicmetadata.artist.ArtistRepository;
import com.iceservices.musicmetadata.track.TrackRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class TrackControllerIntegrationTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JsonMapper jsonMapper;

	@Autowired
	ArtistRepository artistRepository;

	@Autowired
	ArtistAliasRepository artistAliasRepository;

	@Autowired
	TrackRepository trackRepository;

	@BeforeEach
	void cleanDatabase() {
		trackRepository.deleteAll();
		artistAliasRepository.deleteAll();
		artistRepository.deleteAll();
	}

	@Test
	void addsTrackToArtist() throws Exception {
		UUID artistId = createArtist("Massive Attack");

		mockMvc.perform(post("/api/v1/artists/{artistId}/tracks", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "title": "  Teardrop  ",
							  "genre": "Trip hop",
							  "lengthSeconds": 331,
							  "isrc": " gbduw0000053 "
							}
							"""))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/v1/artists/" + artistId + "/tracks")))
				.andExpect(jsonPath("$.artistId").value(artistId.toString()))
				.andExpect(jsonPath("$.title").value("Teardrop"))
				.andExpect(jsonPath("$.genre").value("Trip hop"))
				.andExpect(jsonPath("$.lengthSeconds").value(331))
				.andExpect(jsonPath("$.isrc").value("GBDUW0000053"))
				.andExpect(jsonPath("$._links.artist.href", containsString("/api/v1/artists/" + artistId)))
				.andExpect(jsonPath("$._links.tracks.href", containsString("/tracks")));
	}

	@Test
	void fetchesTracksWithPagination() throws Exception {
		UUID artistId = createArtist("Radiohead");
		addTrack(artistId, "Airbag", 284);
		addTrack(artistId, "Karma Police", 264);
		addTrack(artistId, "No Surprises", 229);

		mockMvc.perform(get("/api/v1/artists/{artistId}/tracks?page=0&size=2", artistId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.artistId").value(artistId.toString()))
				.andExpect(jsonPath("$.tracks", hasSize(2)))
				.andExpect(jsonPath("$.tracks[0].title").value("Airbag"))
				.andExpect(jsonPath("$.tracks[1].title").value("Karma Police"))
				.andExpect(jsonPath("$.page.number").value(0))
				.andExpect(jsonPath("$.page.size").value(2))
				.andExpect(jsonPath("$.page.totalElements").value(3))
				.andExpect(jsonPath("$.page.totalPages").value(2))
				.andExpect(jsonPath("$._links.self.href", containsString("page=0&size=2")))
				.andExpect(jsonPath("$._links.next.href", containsString("page=1&size=2")));

		mockMvc.perform(get("/api/v1/artists/{artistId}/tracks?page=1&size=2", artistId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tracks", hasSize(1)))
				.andExpect(jsonPath("$.tracks[0].title").value("No Surprises"))
				.andExpect(jsonPath("$._links.previous.href", containsString("page=0&size=2")));
	}

	@Test
	void negativePageReturnsValidationFailure() throws Exception {
		UUID artistId = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/artists/{artistId}/tracks?page=-1", artistId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors.page").value("page must not be negative"));
	}

	@Test
	void zeroSizeReturnsValidationFailure() throws Exception {
		UUID artistId = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/artists/{artistId}/tracks?size=0", artistId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors.size").value("size must be positive"));
	}

	@Test
	void tooLargeSizeReturnsValidationFailure() throws Exception {
		UUID artistId = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/artists/{artistId}/tracks?size=101", artistId))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors.size").value("size must be at most 100"));
	}

	@Test
	void trackForMissingArtistReturnsNotFound() throws Exception {
		UUID missingArtistId = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/artists/{artistId}/tracks", missingArtistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(validTrackJson("Missing Track", 180)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Artist not found"))
				.andExpect(jsonPath("$.artistId").value(missingArtistId.toString()));
	}

	@Test
	void tracksForMissingArtistReturnsNotFound() throws Exception {
		UUID missingArtistId = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/artists/{artistId}/tracks", missingArtistId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Artist not found"));
	}

	@Test
	void invalidLengthSecondsReturnsValidationFailure() throws Exception {
		UUID artistId = createArtist("Björk");

		mockMvc.perform(post("/api/v1/artists/{artistId}/tracks", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(validTrackJson("Jóga", 0)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors.lengthSeconds").value("lengthSeconds must be positive"));
	}

	@Test
	void blankTitleReturnsValidationFailure() throws Exception {
		UUID artistId = createArtist("Portishead");

		mockMvc.perform(post("/api/v1/artists/{artistId}/tracks", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(validTrackJson("   ", 200)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors.title").value("title must not be blank"));
	}

	@Test
	void blankGenreIsNormalisedToNull() throws Exception {
		UUID artistId = createArtist("Burial");

		mockMvc.perform(post("/api/v1/artists/{artistId}/tracks", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "title": "Archangel",
							  "genre": "   ",
							  "lengthSeconds": 240
							}
							"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.genre", nullValue()));
	}

	@Test
	void multipleTracksWithoutIsrcAreAllowed() throws Exception {
		UUID artistId = createArtist("Boards of Canada");

		addTrack(artistId, "Alpha and Omega", 417);
		addTrack(artistId, "Music Is Math", 320);

		mockMvc.perform(get("/api/v1/artists/{artistId}/tracks", artistId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tracks", hasSize(2)))
				.andExpect(jsonPath("$.tracks[0].title").value("Alpha and Omega"))
				.andExpect(jsonPath("$.tracks[0].isrc", nullValue()))
				.andExpect(jsonPath("$.tracks[1].title").value("Music Is Math"))
				.andExpect(jsonPath("$.tracks[1].isrc", nullValue()));
	}

	@Test
	void isrcIsUppercaseNormalised() throws Exception {
		UUID artistId = createArtist("Daft Punk");

		mockMvc.perform(post("/api/v1/artists/{artistId}/tracks", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "title": "One More Time",
							  "lengthSeconds": 320,
							  "isrc": " usvir0001107 "
							}
							"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.isrc").value("USVIR0001107"));
	}

	@Test
	void duplicateIsrcReturnsConflict() throws Exception {
		UUID artistId = createArtist("Prince");
		addTrackWithIsrc(artistId, "First Track", "USWB10003013");

		mockMvc.perform(post("/api/v1/artists/{artistId}/tracks", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "title": "Second Track",
							  "lengthSeconds": 250,
							  "isrc": "uswb10003013"
							}
							"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Duplicate track ISRC"))
				.andExpect(jsonPath("$.isrc").value("USWB10003013"));
	}

	@Test
	void malformedArtistIdReturnsBadRequestForTrackEndpoints() throws Exception {
		mockMvc.perform(post("/api/v1/artists/{artistId}/tracks", "not-a-uuid")
					.contentType(MediaType.APPLICATION_JSON)
					.content(validTrackJson("Bad Path", 123)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.parameter").value("artistId"))
				.andExpect(jsonPath("$.invalidValue").value("not-a-uuid"));

		mockMvc.perform(get("/api/v1/artists/{artistId}/tracks", "not-a-uuid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.parameter").value("artistId"))
				.andExpect(jsonPath("$.invalidValue").value("not-a-uuid"));
	}

	private UUID createArtist(String primaryName) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/artists")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"primaryName\":\"" + primaryName + "\"}"))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode json = jsonMapper.readTree(result.getResponse().getContentAsString());
		return UUID.fromString(json.get("id").asText());
	}

	private void addTrack(UUID artistId, String title, int lengthSeconds) throws Exception {
		mockMvc.perform(post("/api/v1/artists/{artistId}/tracks", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(validTrackJson(title, lengthSeconds)))
				.andExpect(status().isCreated());
	}

	private void addTrackWithIsrc(UUID artistId, String title, String isrc) throws Exception {
		mockMvc.perform(post("/api/v1/artists/{artistId}/tracks", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "title": "%s",
							  "lengthSeconds": 200,
							  "isrc": "%s"
							}
							""".formatted(title, isrc)))
				.andExpect(status().isCreated());
	}

	private String validTrackJson(String title, int lengthSeconds) {
		return """
				{
				  "title": "%s",
				  "lengthSeconds": %d
				}
				""".formatted(title, lengthSeconds);
	}
}
