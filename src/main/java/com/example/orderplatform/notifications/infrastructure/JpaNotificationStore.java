package com.example.orderplatform.notifications.infrastructure;

import com.example.orderplatform.notifications.api.NotificationSummary;
import com.example.orderplatform.notifications.application.NotificationStore;
import com.example.orderplatform.notifications.domain.NotificationDraft;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
class JpaNotificationStore implements NotificationStore {

    private final NotificationRepository notifications;

    JpaNotificationStore(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Override
    public void save(NotificationDraft draft) {
        notifications.save(NotificationEntity.ready(draft));
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
