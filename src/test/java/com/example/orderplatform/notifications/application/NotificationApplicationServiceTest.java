package com.example.orderplatform.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.orderplatform.notifications.api.NotificationSummary;
import com.example.orderplatform.notifications.domain.NotificationDraft;
import com.example.orderplatform.orders.api.OrderCreatedEvent;
import com.example.orderplatform.payments.api.PaymentAuthorizedEvent;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationApplicationServiceTest {

    @Mock
    NotificationStore notifications;

    @Test
    void recordsNotificationDraftsForDomainEvents() {
        var service = new NotificationApplicationService(notifications);
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        service.on(new OrderCreatedEvent(
                orderId,
                UUID.randomUUID(),
                new BigDecimal("49.97"),
                "EUR",
                OffsetDateTime.now()));
        service.on(new PaymentAuthorizedEvent(
                paymentId,
                UUID.randomUUID(),
                new BigDecimal("49.97"),
                "EUR",
                OffsetDateTime.now()));

        ArgumentCaptor<NotificationDraft> drafts = ArgumentCaptor.forClass(NotificationDraft.class);
        ArgumentCaptor<UUID> sources = ArgumentCaptor.forClass(UUID.class);
        verify(notifications, org.mockito.Mockito.times(2)).save(sources.capture(), drafts.capture());
        assertThat(sources.getAllValues()).containsExactly(orderId, paymentId);
        assertThat(drafts.getAllValues())
                .extracting(NotificationDraft::type)
                .containsExactly("ORDER_CREATED", "PAYMENT_AUTHORIZED");
    }

    @Test
    void listsNotificationsThroughStorePort() {
        var summary = new NotificationSummary(
                UUID.randomUUID(),
                "operations",
                "WEBHOOK",
                "PAYMENT_AUTHORIZED",
                "READY",
                OffsetDateTime.now());
        when(notifications.findRecent()).thenReturn(List.of(summary));

        var service = new NotificationApplicationService(notifications);

        assertThat(service.listRecent()).containsExactly(summary);
    }
}
