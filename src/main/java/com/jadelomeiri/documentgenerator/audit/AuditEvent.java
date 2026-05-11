package com.jadelomeiri.documentgenerator.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "audit_events")
public class AuditEvent {

	@Id
	@GeneratedValue
	@UuidGenerator
	@Column(nullable = false, updatable = false)
	private UUID id;

	@Column(name = "event_type", nullable = false, length = 100)
	private String eventType;

	@Column(name = "target_type", nullable = false, length = 100)
	private String targetType;

	@Column(name = "target_id", nullable = false)
	private UUID targetId;

	@Column(name = "actor_reference", nullable = false, length = 255)
	private String actorReference;

	@Column(name = "details_json", columnDefinition = "TEXT")
	private String detailsJson;

	@Column(name = "occurred_at", nullable = false, updatable = false)
	private Instant occurredAt;

	protected AuditEvent() {
	}

	public AuditEvent(String eventType, String targetType, UUID targetId, String actorReference, String detailsJson) {
		this.eventType = eventType;
		this.targetType = targetType;
		this.targetId = targetId;
		this.actorReference = actorReference;
		this.detailsJson = detailsJson;
	}

	public UUID getId() {
		return id;
	}

	public String getEventType() {
		return eventType;
	}

	public String getTargetType() {
		return targetType;
	}

	public UUID getTargetId() {
		return targetId;
	}

	public String getActorReference() {
		return actorReference;
	}

	public String getDetailsJson() {
		return detailsJson;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	@PrePersist
	void onCreate() {
		occurredAt = Instant.now();
	}
}
