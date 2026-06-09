package com.example.orderplatform.notifications.api;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationSummary(
        UUID id,
        String recipient,
        String channel,
        String type,
        String status,
        OffsetDateTime createdAt) {
}
