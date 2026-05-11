package com.jadelomeiri.documentgenerator.document;

import com.jadelomeiri.documentgenerator.generation.DocumentGenerationRequest;
import com.jadelomeiri.documentgenerator.template.DocumentTemplateVersion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "generated_documents")
public class GeneratedDocument {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(nullable = false, updatable = false)
	private UUID id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "generation_request_id", nullable = false)
	private DocumentGenerationRequest generationRequest;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "template_version_id", nullable = false)
	private DocumentTemplateVersion templateVersion;

	@Column(name = "content_type", nullable = false, length = 100)
	private String contentType;

	@Column(length = 128)
	private String checksum;

	@Column(name = "storage_reference", nullable = false, length = 500)
	private String storageReference;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected GeneratedDocument() {
	}

	public GeneratedDocument(
			DocumentGenerationRequest generationRequest,
			DocumentTemplateVersion templateVersion,
			String contentType,
			String checksum,
			String storageReference) {
		this.generationRequest = generationRequest;
		this.templateVersion = templateVersion;
		this.contentType = contentType;
		this.checksum = checksum;
		this.storageReference = storageReference;
	}

	public UUID getId() {
		return id;
	}

	public DocumentGenerationRequest getGenerationRequest() {
		return generationRequest;
	}

	public DocumentTemplateVersion getTemplateVersion() {
		return templateVersion;
	}

	public String getContentType() {
		return contentType;
	}

	public String getChecksum() {
		return checksum;
	}

	public String getStorageReference() {
		return storageReference;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}
}
