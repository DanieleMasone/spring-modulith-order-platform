package com.example.orderplatform.notifications.infrastructure;

import com.example.orderplatform.notifications.api.NotificationSummary;
import com.example.orderplatform.notifications.application.NotificationStore;
import com.example.orderplatform.notifications.domain.NotificationDraft;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class JpaNotificationStore implements NotificationStore {

    private final NotificationRepository notifications;

    JpaNotificationStore(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Override
    public void save(UUID sourceId, NotificationDraft draft) {
        notifications.insertIfAbsent(UUID.randomUUID(), sourceId, draft.recipient(), draft.channel(),
                draft.type(), draft.payload(), OffsetDateTime.now());
    }

    @Override
    public List<NotificationSummary> findRecent() {
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
