package com.jadelomeiri.documentgenerator.generation;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadelomeiri.documentgenerator.TestcontainersConfiguration;
import com.jadelomeiri.documentgenerator.audit.AuditEventRepository;
import com.jadelomeiri.documentgenerator.document.GeneratedDocumentRepository;
import com.jadelomeiri.documentgenerator.template.DocumentTemplateVersionRepository;
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
@SpringBootTest
@AutoConfigureMockMvc
class DocumentGeneratorApiIntegrationTests {

	private static final UUID LOAN_AGREEMENT_TEMPLATE_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
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

	@Autowired
	DocumentTemplateVersionRepository templateVersionRepository;

	@BeforeEach
	void cleanDatabase() {
		generatedDocumentRepository.deleteAll();
		generationRequestRepository.deleteAll();
		auditEventRepository.deleteAll();
	}

	@Test
	void listsSeededTemplates() throws Exception {
		mockMvc.perform(get("/api/v1/templates"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.templates", hasSize(2)))
				.andExpect(jsonPath("$.templates[*].name", containsInAnyOrder("Loan Agreement", "Customer Statement")))
				.andExpect(jsonPath("$.templates[*].active", containsInAnyOrder(true, true)));
	}

	@Test
	void listsTemplateVersions() throws Exception {
		mockMvc.perform(get("/api/v1/templates/{templateId}/versions", LOAN_AGREEMENT_TEMPLATE_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.templateId").value(LOAN_AGREEMENT_TEMPLATE_ID.toString()))
				.andExpect(jsonPath("$.versions", hasSize(1)))
				.andExpect(jsonPath("$.versions[0].id").value(LOAN_AGREEMENT_VERSION_ID.toString()))
				.andExpect(jsonPath("$.versions[0].format").value("PDF"))
				.andExpect(jsonPath("$.versions[0].status").value("ACTIVE"));
	}

	@Test
	void createsGenerationRequestAndGeneratedDocumentWithAuditEvents() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/v1/generation-requests")
					.contentType(MediaType.APPLICATION_JSON)
					.content(generationRequestJson(LOAN_AGREEMENT_VERSION_ID)))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/v1/generation-requests/")))
				.andExpect(jsonPath("$.templateVersionId").value(LOAN_AGREEMENT_VERSION_ID.toString()))
				.andExpect(jsonPath("$.customerReference").value("customer-123"))
				.andExpect(jsonPath("$.requestedBy").value("caseworker-456"))
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.generatedDocument.templateVersionId").value(LOAN_AGREEMENT_VERSION_ID.toString()))
				.andExpect(jsonPath("$.generatedDocument.contentType").value("application/pdf"))
				.andExpect(jsonPath("$.generatedDocument.storageReference", containsString("demo://generated-documents/")))
				.andReturn();

		JsonNode response = objectMapper.readTree(createResult.getResponse().getContentAsString());
		UUID requestId = UUID.fromString(response.get("id").asText());
		UUID generatedDocumentId = UUID.fromString(response.get("generatedDocument").get("id").asText());

		mockMvc.perform(get("/api/v1/generation-requests/{requestId}", requestId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(requestId.toString()))
				.andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.generatedDocument.id").value(generatedDocumentId.toString()));

		mockMvc.perform(get("/api/v1/generated-documents/{documentId}", generatedDocumentId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(generatedDocumentId.toString()))
				.andExpect(jsonPath("$.generationRequestId").value(requestId.toString()))
				.andExpect(jsonPath("$.templateVersionId").value(LOAN_AGREEMENT_VERSION_ID.toString()));

		mockMvc.perform(get("/api/v1/generation-requests/{requestId}/audit-events", requestId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.auditEvents", hasSize(2)))
				.andExpect(jsonPath("$.auditEvents[*].eventType",
						containsInAnyOrder("GENERATION_REQUEST_CREATED", "GENERATION_COMPLETED")));
	}

	@Test
	void missingTemplateVersionReturnsNotFound() throws Exception {
		UUID missingTemplateVersionId = UUID.randomUUID();

		mockMvc.perform(post("/api/v1/generation-requests")
					.contentType(MediaType.APPLICATION_JSON)
					.content(generationRequestJson(missingTemplateVersionId)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.title").value("Document template version not found"))
				.andExpect(jsonPath("$.templateVersionId").value(missingTemplateVersionId.toString()));
	}

	@Test
	void seededGeneratedDocumentStoresTemplateVersion() throws Exception {
		MvcResult createResult = mockMvc.perform(post("/api/v1/generation-requests")
					.contentType(MediaType.APPLICATION_JSON)
					.content(generationRequestJson(LOAN_AGREEMENT_VERSION_ID)))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode response = objectMapper.readTree(createResult.getResponse().getContentAsString());
		UUID generatedDocumentId = UUID.fromString(response.get("generatedDocument").get("id").asText());

		assertThat(templateVersionRepository.existsById(LOAN_AGREEMENT_VERSION_ID)).isTrue();
		assertThat(generatedDocumentRepository.findById(generatedDocumentId)
				.orElseThrow()
				.getTemplateVersion()
				.getId()).isEqualTo(LOAN_AGREEMENT_VERSION_ID);
	}

	private String generationRequestJson(UUID templateVersionId) {
		return """
				{
				  "templateVersionId": "%s",
				  "customerReference": "customer-123",
				  "requestedBy": "caseworker-456",
				  "inputPayloadJson": "{\"loanAmount\":125000,\"currency\":\"GBP\"}"
				}
				""".formatted(templateVersionId);
	}
}
