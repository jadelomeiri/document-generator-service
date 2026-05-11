package com.jadelomeiri.documentgenerator.generation;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadelomeiri.documentgenerator.TestcontainersConfiguration;
import com.jadelomeiri.documentgenerator.audit.AuditEventRepository;
import com.jadelomeiri.documentgenerator.document.GeneratedDocumentDraft;
import com.jadelomeiri.documentgenerator.document.GeneratedDocumentRepository;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Import({TestcontainersConfiguration.class, GenerationFailureIntegrationTests.FailingGenerationConfiguration.class})
@SpringBootTest
@AutoConfigureMockMvc
class GenerationFailureIntegrationTests {

	private static final UUID LOAN_AGREEMENT_VERSION_ID = UUID.fromString("20000000-0000-0000-0000-000000000001");

	@Autowired
	MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	AuditEventRepository auditEventRepository;

	@Autowired
	GeneratedDocumentRepository generatedDocumentRepository;

	@Autowired
	DocumentGenerationRequestRepository generationRequestRepository;

	@BeforeEach
	void cleanDatabase() {
		generatedDocumentRepository.deleteAll();
		generationRequestRepository.deleteAll();
		auditEventRepository.deleteAll();
	}

	@Test
	void generationFailureMarksRequestFailedAndCreatesAuditEvent() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/v1/generation-requests")
					.contentType(MediaType.APPLICATION_JSON)
					.content(generationRequestJson()))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("FAILED"))
				.andExpect(jsonPath("$.failureReason").value("demo renderer failed"))
				.andExpect(jsonPath("$.generatedDocument").doesNotExist())
				.andReturn();

		JsonNode response = objectMapper.readTree(createResult.getResponse().getContentAsString());
		UUID requestId = UUID.fromString(response.get("id").asText());

		mockMvc.perform(get("/api/v1/generation-requests/{requestId}", requestId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("FAILED"))
				.andExpect(jsonPath("$.failureReason").value("demo renderer failed"));

		mockMvc.perform(get("/api/v1/generation-requests/{requestId}/audit-events", requestId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.auditEvents", hasSize(2)))
				.andExpect(jsonPath("$.auditEvents[*].eventType",
						containsInAnyOrder("GENERATION_REQUEST_CREATED", "GENERATION_FAILED")));
	}

	private String generationRequestJson() throws Exception {
		return objectMapper.writeValueAsString(Map.of(
				"templateVersionId", LOAN_AGREEMENT_VERSION_ID,
				"customerReference", "customer-123",
				"requestedBy", "caseworker-456",
				"inputPayloadJson", "{\"loanAmount\":125000,\"currency\":\"GBP\"}"));
	}

	@TestConfiguration
	static class FailingGenerationConfiguration {

		@Bean
		@Primary
		DocumentGenerationService failingDocumentGenerationService() {
			return new DocumentGenerationService() {
				@Override
				public GeneratedDocumentDraft generate(DocumentGenerationRequest request) {
					throw new IllegalStateException("demo renderer failed");
				}
			};
		}
	}
}
