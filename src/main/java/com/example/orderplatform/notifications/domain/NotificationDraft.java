package com.example.orderplatform.notifications.domain;

import com.example.orderplatform.BusinessRuleViolationException;
import java.util.Locale;

/**
 * Validated notification intent before it is persisted.
 *
 * <p>The module records intents for email and webhook channels; it does not deliver messages to
 * external systems.
 *
 * @param recipient logical recipient
 * @param channel supported channel, either {@code EMAIL} or {@code WEBHOOK}
 * @param type notification business type
 * @param payload notification payload text
 */
public record NotificationDraft(String recipient, String channel, String type, String payload) {

    public NotificationDraft {
        recipient = requireText(recipient, "Notification recipient is required.");
        channel = requireText(channel, "Notification channel is required.").toUpperCase(Locale.ROOT);
        type = requireText(type, "Notification type is required.").toUpperCase(Locale.ROOT);
        payload = requireText(payload, "Notification payload is required.");

        if (!channel.equals("EMAIL") && !channel.equals("WEBHOOK")) {
            throw new BusinessRuleViolationException("Notification channel must be EMAIL or WEBHOOK.");
        }
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleViolationException(message);
        }
        return value.strip();
    }
}
