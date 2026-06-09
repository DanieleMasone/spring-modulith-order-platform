package com.example.orderplatform.notifications.infrastructure;

import com.example.orderplatform.notifications.domain.NotificationDraft;
import com.example.orderplatform.notifications.domain.NotificationStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    private UUID id;
    private String recipient;
    private String channel;
    private String type;
    private String payload;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private OffsetDateTime createdAt;

    protected NotificationEntity() {
    }

    private NotificationEntity(UUID id, String recipient, String channel, String type, String payload, NotificationStatus status, OffsetDateTime createdAt) {
        this.id = id;
        this.recipient = recipient;
        this.channel = channel;
        this.type = type;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static NotificationEntity ready(String recipient, String channel, String type, String payload) {
        return ready(new NotificationDraft(recipient, channel, type, payload));
    }

    public static NotificationEntity ready(NotificationDraft draft) {
        return new NotificationEntity(
                UUID.randomUUID(),
                draft.recipient(),
                draft.channel(),
                draft.type(),
                draft.payload(),
                NotificationStatus.READY,
                OffsetDateTime.now());
    }

    public UUID id() {
        return id;
    }

    public String recipient() {
        return recipient;
    }

    public String channel() {
        return channel;
    }

    public String type() {
        return type;
    }

    public NotificationStatus status() {
        return status;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }
}
