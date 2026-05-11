package com.jadelomeiri.documentgenerator.artist.api;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.jadelomeiri.documentgenerator.TestcontainersConfiguration;
import com.jadelomeiri.documentgenerator.artist.ArtistAliasRepository;
import com.jadelomeiri.documentgenerator.artist.ArtistRepository;
import com.jadelomeiri.documentgenerator.track.TrackRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ArtistControllerIntegrationTests {

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
	void createsGetsAndUpdatesArtist() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/v1/artists")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"primaryName\":\"  Aphex Twin  \"}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/v1/artists/")))
				.andExpect(jsonPath("$.primaryName").value("Aphex Twin"))
				.andExpect(jsonPath("$._links.self.href", containsString("/api/v1/artists/")))
				.andExpect(jsonPath("$._links.aliases.href", containsString("/aliases")))
				.andExpect(jsonPath("$._links.tracks.href", containsString("/tracks")))
				.andReturn();

		UUID artistId = readId(createResult);

		mockMvc.perform(get("/api/v1/artists/{artistId}", artistId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(artistId.toString()))
				.andExpect(jsonPath("$.primaryName").value("Aphex Twin"));

		mockMvc.perform(patch("/api/v1/artists/{artistId}", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"primaryName\":\"AFX\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(artistId.toString()))
				.andExpect(jsonPath("$.primaryName").value("AFX"));
	}

	@Test
	void addsAndListsAliases() throws Exception {
		UUID artistId = createArtist("Richard D. James");

		mockMvc.perform(post("/api/v1/artists/{artistId}/aliases", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"alias\":\"  Polygon Window  \"}"))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/v1/artists/" + artistId + "/aliases")))
				.andExpect(jsonPath("$.artistId").value(artistId.toString()))
				.andExpect(jsonPath("$.alias").value("Polygon Window"));

		mockMvc.perform(post("/api/v1/artists/{artistId}/aliases", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"alias\":\"Caustic Window\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/v1/artists/{artistId}/aliases", artistId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.artistId").value(artistId.toString()))
				.andExpect(jsonPath("$.aliases", hasSize(2)))
				.andExpect(jsonPath("$.aliases[0].alias").value("Caustic Window"))
				.andExpect(jsonPath("$.aliases[1].alias").value("Polygon Window"));
	}

	@Test
	void rejectsBlankArtistName() throws Exception {
		mockMvc.perform(post("/api/v1/artists")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"primaryName\":\"   \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors.primaryName").value("primaryName must not be blank"));
	}

	@Test
	void missingArtistReturnsNotFound() throws Exception {
		UUID missingArtistId = UUID.randomUUID();

		mockMvc.perform(get("/api/v1/artists/{artistId}", missingArtistId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Artist not found"))
				.andExpect(jsonPath("$.artistId").value(missingArtistId.toString()));
	}

	@Test
	void malformedArtistIdReturnsBadRequest() throws Exception {
		mockMvc.perform(get("/api/v1/artists/{artistId}", "not-a-uuid"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Invalid path parameter"))
				.andExpect(jsonPath("$.detail").value("Parameter 'artistId' must be a valid UUID."))
				.andExpect(jsonPath("$.parameter").value("artistId"))
				.andExpect(jsonPath("$.invalidValue").value("not-a-uuid"));
	}

	@Test
	void duplicateAliasForSameArtistReturnsConflict() throws Exception {
		UUID artistId = createArtist("Prince");

		mockMvc.perform(post("/api/v1/artists/{artistId}/aliases", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"alias\":\"The Artist\"}"))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/artists/{artistId}/aliases", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"alias\":\"the artist\"}"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.title").value("Duplicate artist alias"))
				.andExpect(jsonPath("$.artistId").value(artistId.toString()))
				.andExpect(jsonPath("$.alias").value("the artist"));
	}

	@Test
	void blankAliasReturnsValidationFailure() throws Exception {
		UUID artistId = createArtist("Beyoncé");

		mockMvc.perform(post("/api/v1/artists/{artistId}/aliases", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"alias\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Validation failed"))
				.andExpect(jsonPath("$.errors.alias").value("alias must not be blank"));
	}

	@Test
	void aliasForMissingArtistReturnsNotFound() throws Exception {
		UUID missingArtistId = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/artists/{artistId}/aliases", missingArtistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"alias\":\"Missing Alias\"}"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Artist not found"));
	}

	private UUID createArtist(String primaryName) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/artists")
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"primaryName\":\"" + primaryName + "\"}"))
				.andExpect(status().isCreated())
				.andReturn();
		return readId(result);
	}

	private UUID readId(MvcResult result) throws Exception {
		JsonNode json = jsonMapper.readTree(result.getResponse().getContentAsString());
		return UUID.fromString(json.get("id").asText());
	}
}
