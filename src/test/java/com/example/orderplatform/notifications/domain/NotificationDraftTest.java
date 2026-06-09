package com.example.orderplatform.notifications.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.orderplatform.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class NotificationDraftTest {

    @Test
    void normalizesNotificationMetadata() {
        var draft = new NotificationDraft(" operations ", "webhook", "payment_authorized", " Payment authorized. ");

        assertThat(draft.recipient()).isEqualTo("operations");
        assertThat(draft.channel()).isEqualTo("WEBHOOK");
        assertThat(draft.type()).isEqualTo("PAYMENT_AUTHORIZED");
        assertThat(draft.payload()).isEqualTo("Payment authorized.");
    }

    @Test
    void rejectsUnsupportedChannels() {
        assertThatThrownBy(() -> new NotificationDraft("operations", "SMS", "PAYMENT_AUTHORIZED", "Payment authorized."))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("EMAIL or WEBHOOK");
    }
}
