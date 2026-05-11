package com.jadelomeiri.documentgenerator.template;

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
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "document_template_versions")
public class DocumentTemplateVersion {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "template_id", nullable = false)
	private DocumentTemplate template;

	@Column(name = "version_number", nullable = false)
	private int versionNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private DocumentFormat format;

	@Column(name = "template_location", nullable = false, length = 500)
	private String templateLocation;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TemplateVersionStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "activated_at")
	private Instant activatedAt;

	protected DocumentTemplateVersion() {
	}

	public DocumentTemplateVersion(
			DocumentTemplate template,
			int versionNumber,
			DocumentFormat format,
			String templateLocation,
			TemplateVersionStatus status,
			Instant activatedAt) {
		this.template = template;
		this.versionNumber = versionNumber;
		this.format = format;
		this.templateLocation = templateLocation;
		this.status = status;
		this.activatedAt = activatedAt;
	}

	public UUID getId() {
		return id;
	}

	public DocumentTemplate getTemplate() {
		return template;
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public DocumentFormat getFormat() {
		return format;
	}

	public String getTemplateLocation() {
		return templateLocation;
	}

	public TemplateVersionStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getActivatedAt() {
		return activatedAt;
	}

	@PrePersist
	void onCreate() {
		createdAt = Instant.now();
	}
}
