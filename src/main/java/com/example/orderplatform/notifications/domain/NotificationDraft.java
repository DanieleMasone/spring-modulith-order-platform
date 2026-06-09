package com.example.orderplatform.notifications.domain;

import com.example.orderplatform.BusinessRuleViolationException;

public record NotificationDraft(String recipient, String channel, String type, String payload) {

    public NotificationDraft {
        recipient = requireText(recipient, "Notification recipient is required.");
        channel = requireText(channel, "Notification channel is required.").toUpperCase();
        type = requireText(type, "Notification type is required.").toUpperCase();
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
