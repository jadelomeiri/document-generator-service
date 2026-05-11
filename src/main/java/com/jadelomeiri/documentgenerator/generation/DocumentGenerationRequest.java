package com.jadelomeiri.documentgenerator.generation;

import com.jadelomeiri.documentgenerator.template.DocumentTemplateVersion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "document_generation_requests")
public class DocumentGenerationRequest {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "template_version_id", nullable = false)
	private DocumentTemplateVersion templateVersion;

	@Column(name = "customer_reference", nullable = false, length = 255)
	private String customerReference;

	@Column(name = "requested_by", nullable = false, length = 255)
	private String requestedBy;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private GenerationRequestStatus status;

	@Column(name = "input_payload_json", nullable = false, columnDefinition = "TEXT")
	private String inputPayloadJson;

	@Column(name = "failure_reason", columnDefinition = "TEXT")
	private String failureReason;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	protected DocumentGenerationRequest() {
	}

	public DocumentGenerationRequest(
			DocumentTemplateVersion templateVersion,
			String customerReference,
			String requestedBy,
			String inputPayloadJson) {
		this.templateVersion = templateVersion;
		this.customerReference = customerReference;
		this.requestedBy = requestedBy;
		this.status = GenerationRequestStatus.RECEIVED;
		this.inputPayloadJson = inputPayloadJson;
	}

	public UUID getId() {
		return id;
	}

	public DocumentTemplateVersion getTemplateVersion() {
		return templateVersion;
	}

	public String getCustomerReference() {
		return customerReference;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public GenerationRequestStatus getStatus() {
		return status;
	}

	public String getInputPayloadJson() {
		return inputPayloadJson;
	}

	public String getFailureReason() {
		return failureReason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public void markValidated() {
		status = GenerationRequestStatus.VALIDATED;
	}

	public void markGenerating() {
		status = GenerationRequestStatus.GENERATING;
	}

	public void markCompleted(Instant completedAt) {
		status = GenerationRequestStatus.COMPLETED;
		this.completedAt = completedAt;
		failureReason = null;
	}

	public void markFailed(String failureReason) {
		status = GenerationRequestStatus.FAILED;
		this.failureReason = failureReason;
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
}
