package com.example.orderplatform.notifications.api;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Immutable view of a recorded notification intent.
 *
 * @param id notification identifier
 * @param recipient logical recipient
 * @param channel delivery channel type
 * @param type notification business type
 * @param status current notification status
 * @param createdAt creation timestamp
 */
public record NotificationSummary(
        UUID id,
        String recipient,
        String channel,
        String type,
        String status,
        OffsetDateTime createdAt) {
}
