package com.jadelomeiri.documentgenerator.homepage.api;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jadelomeiri.documentgenerator.TestcontainersConfiguration;
import com.jadelomeiri.documentgenerator.artist.ArtistAliasRepository;
import com.jadelomeiri.documentgenerator.artist.ArtistRepository;
import com.jadelomeiri.documentgenerator.track.TrackRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Import({ TestcontainersConfiguration.class, HomepageControllerIntegrationTests.ClockTestConfiguration.class })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class HomepageControllerIntegrationTests {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JsonMapper jsonMapper;

	@Autowired
	MutableClock clock;

	@Autowired
	ArtistRepository artistRepository;

	@Autowired
	ArtistAliasRepository artistAliasRepository;

	@Autowired
	TrackRepository trackRepository;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@BeforeEach
	void cleanDatabase() {
		trackRepository.deleteAll();
		artistAliasRepository.deleteAll();
		artistRepository.deleteAll();
		clock.setInstant(Instant.parse("2024-01-01T12:00:00Z"));
		clock.setZone(ZoneOffset.UTC);
	}

	@Test
	void noArtistsReturnsNotFoundProblemDetail() throws Exception {
		mockMvc.perform(get("/api/v1/homepage/artist-of-the-day"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Artist of the Day unavailable"))
				.andExpect(jsonPath("$.detail")
						.value("Artist of the Day cannot be selected because no canonical artists exist."));
	}

	@Test
	void sameUtcDateReturnsSameArtist() throws Exception {
		UUID firstArtistId = createArtist("Aphex Twin");
		UUID secondArtistId = createArtist("Björk");
		UUID thirdArtistId = createArtist("Daft Punk");
		setCreatedAt(firstArtistId, "2024-01-01T00:00:01Z");
		setCreatedAt(secondArtistId, "2024-01-01T00:00:02Z");
		setCreatedAt(thirdArtistId, "2024-01-01T00:00:03Z");

		clock.setInstant(Instant.parse("2024-01-02T00:01:00Z"));
		mockMvc.perform(get("/api/v1/homepage/artist-of-the-day"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(secondArtistId.toString()))
				.andExpect(jsonPath("$.primaryName").value("Björk"));

		clock.setInstant(Instant.parse("2024-01-02T23:59:00Z"));
		mockMvc.perform(get("/api/v1/homepage/artist-of-the-day"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(secondArtistId.toString()))
				.andExpect(jsonPath("$.primaryName").value("Björk"));
	}

	@Test
	void nextUtcDayRotatesToNextArtist() throws Exception {
		UUID firstArtistId = createArtist("Massive Attack");
		UUID secondArtistId = createArtist("Portishead");
		UUID thirdArtistId = createArtist("Radiohead");
		setCreatedAt(firstArtistId, "2024-01-01T00:00:01Z");
		setCreatedAt(secondArtistId, "2024-01-01T00:00:02Z");
		setCreatedAt(thirdArtistId, "2024-01-01T00:00:03Z");

		clock.setInstant(Instant.parse("2024-01-02T10:00:00Z"));
		mockMvc.perform(get("/api/v1/homepage/artist-of-the-day"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(secondArtistId.toString()));

		clock.setInstant(Instant.parse("2024-01-03T10:00:00Z"));
		mockMvc.perform(get("/api/v1/homepage/artist-of-the-day"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(thirdArtistId.toString()));
	}

	@Test
	void aliasesDoNotAffectRotation() throws Exception {
		UUID firstArtistId = createArtist("Richard D. James");
		UUID secondArtistId = createArtist("Squarepusher");
		setCreatedAt(firstArtistId, "2024-01-01T00:00:01Z");
		setCreatedAt(secondArtistId, "2024-01-01T00:00:02Z");
		addAlias(firstArtistId, "AFX");
		addAlias(firstArtistId, "Polygon Window");
		addAlias(firstArtistId, "Caustic Window");

		clock.setInstant(Instant.parse("2024-01-02T10:00:00Z"));
		mockMvc.perform(get("/api/v1/homepage/artist-of-the-day"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(secondArtistId.toString()))
				.andExpect(jsonPath("$.primaryName").value("Squarepusher"));
	}

	@Test
	void injectedClockUtcDateControlsSelectedDay() throws Exception {
		UUID firstArtistId = createArtist("Burial");
		UUID secondArtistId = createArtist("Four Tet");
		setCreatedAt(firstArtistId, "2024-01-01T00:00:01Z");
		setCreatedAt(secondArtistId, "2024-01-01T00:00:02Z");

		clock.setZone(ZoneId.of("America/Los_Angeles"));
		clock.setInstant(Instant.parse("2024-01-02T00:30:00Z"));

		mockMvc.perform(get("/api/v1/homepage/artist-of-the-day"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(secondArtistId.toString()))
				.andExpect(jsonPath("$.primaryName").value("Four Tet"));
	}

	@Test
	void orderingIsStableByCreatedAtThenId() throws Exception {
		UUID higherId = UUID.fromString("00000000-0000-0000-0000-000000000002");
		UUID lowerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
		insertArtist(higherId, "Higher UUID", "2024-01-01T00:00:00Z");
		insertArtist(lowerId, "Lower UUID", "2024-01-01T00:00:00Z");

		clock.setInstant(Instant.parse("2024-01-01T12:00:00Z"));

		mockMvc.perform(get("/api/v1/homepage/artist-of-the-day"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(lowerId.toString()))
				.andExpect(jsonPath("$.primaryName").value("Lower UUID"));
	}

	@Test
	void endpointResponseIncludesUsefulLinks() throws Exception {
		UUID artistId = createArtist("Nina Simone");
		setCreatedAt(artistId, "2024-01-01T00:00:01Z");

		MvcResult result = mockMvc.perform(get("/api/v1/homepage/artist-of-the-day"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(artistId.toString()))
				.andExpect(jsonPath("$._links.self.href", containsString("/api/v1/artists/" + artistId)))
				.andExpect(jsonPath("$._links.artistOfTheDay.href", containsString("/api/v1/homepage/artist-of-the-day")))
				.andExpect(jsonPath("$._links.aliases.href", containsString("/api/v1/artists/" + artistId + "/aliases")))
				.andExpect(jsonPath("$._links.tracks.href", containsString("/api/v1/artists/" + artistId + "/tracks")))
				.andReturn();

		JsonNode json = jsonMapper.readTree(result.getResponse().getContentAsString());
		UUID.fromString(json.get("id").asText());
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

	private void addAlias(UUID artistId, String alias) throws Exception {
		mockMvc.perform(post("/api/v1/artists/{artistId}/aliases", artistId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"alias\":\"" + alias + "\"}"))
				.andExpect(status().isCreated());
	}

	private void setCreatedAt(UUID artistId, String createdAt) {
		OffsetDateTime timestamp = Instant.parse(createdAt).atOffset(ZoneOffset.UTC);

		jdbcTemplate.update("""
            update artists
            set created_at = ?, updated_at = ?
            where id = ?
            """,
				timestamp,
				timestamp,
				artistId);
	}

	private void insertArtist(UUID artistId, String primaryName, String createdAt) {
		OffsetDateTime timestamp = Instant.parse(createdAt).atOffset(ZoneOffset.UTC);

		jdbcTemplate.update("""
            insert into artists (id, primary_name, created_at, updated_at)
            values (?, ?, ?, ?)
            """,
				artistId,
				primaryName,
				timestamp,
				timestamp);
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class ClockTestConfiguration {

		@Bean
		@Primary
		MutableClock mutableClock() {
			return new MutableClock(Instant.parse("2024-01-01T12:00:00Z"), ZoneOffset.UTC);
		}
	}

	static class MutableClock extends Clock {

		private Instant instant;
		private ZoneId zone;

		MutableClock(Instant instant, ZoneId zone) {
			this.instant = instant;
			this.zone = zone;
		}

		void setInstant(Instant instant) {
			this.instant = instant;
		}

		void setZone(ZoneId zone) {
			this.zone = zone;
		}

		@Override
		public ZoneId getZone() {
			return zone;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return new MutableClock(instant, zone);
		}

		@Override
		public Instant instant() {
			return instant;
		}
	}
}
