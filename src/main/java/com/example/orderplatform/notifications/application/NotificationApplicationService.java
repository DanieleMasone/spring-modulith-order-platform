package com.example.orderplatform.notifications.application;

import com.example.orderplatform.notifications.api.NotificationLog;
import com.example.orderplatform.notifications.api.NotificationSummary;
import com.example.orderplatform.notifications.domain.NotificationDraft;
import com.example.orderplatform.notifications.infrastructure.NotificationEntity;
import com.example.orderplatform.notifications.infrastructure.NotificationRepository;
import com.example.orderplatform.orders.api.OrderCreatedEvent;
import com.example.orderplatform.payments.api.PaymentAuthorizedEvent;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Application service that records notification intents after committed order and payment events.
 */
@Service
@Transactional
public class NotificationApplicationService implements NotificationLog {

    private final NotificationRepository notifications;

    public NotificationApplicationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    /**
     * Records a customer notification intent for a committed order.
     *
     * @param event committed order event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(OrderCreatedEvent event) {
        notifications.save(NotificationEntity.ready(new NotificationDraft(
                "customer:" + event.customerId(),
                "EMAIL",
                "ORDER_CREATED",
                "Order " + event.orderId() + " was submitted for " + event.totalAmount() + " " + event.currency() + ".")));
    }

    /**
     * Records an operational notification intent for an authorized payment.
     *
     * @param event payment authorization event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(PaymentAuthorizedEvent event) {
        notifications.save(NotificationEntity.ready(new NotificationDraft(
                "operations",
                "WEBHOOK",
                "PAYMENT_AUTHORIZED",
                "Payment " + event.paymentId() + " was authorized for order " + event.orderId() + ".")));
    }

    /**
     * Lists recently recorded notification intents.
     *
     * @return notification summaries in reverse creation order
     */
    @Override
    @Transactional(readOnly = true)
    public List<NotificationSummary> listRecent() {
        return notifications.findTop50ByOrderByCreatedAtDesc().stream()
                .map(this::toSummary)
                .toList();
    }

    private NotificationSummary toSummary(NotificationEntity notification) {
        return new NotificationSummary(
                notification.id(),
                notification.recipient(),
                notification.channel(),
                notification.type(),
                notification.status().name(),
                notification.createdAt());
    }
}
