package com.example.orderplatform.notifications.infrastructure;

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
