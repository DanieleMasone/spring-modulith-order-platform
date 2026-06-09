package com.example.orderplatform.notifications.api;

import com.example.orderplatform.generated.api.NotificationsApi;
import com.example.orderplatform.generated.model.NotificationResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class NotificationRestController implements NotificationsApi {

    private final NotificationLog notifications;

    NotificationRestController(NotificationLog notifications) {
        this.notifications = notifications;
    }

    @Override
    public ResponseEntity<List<NotificationResponse>> listNotifications() {
        return ResponseEntity.ok(notifications.listRecent().stream()
                .map(this::toResponse)
                .toList());
    }

    private NotificationResponse toResponse(NotificationSummary notification) {
        return new NotificationResponse()
                .id(notification.id())
                .recipient(notification.recipient())
                .channel(NotificationResponse.ChannelEnum.valueOf(notification.channel()))
                .type(notification.type())
                .status(NotificationResponse.StatusEnum.valueOf(notification.status()))
                .createdAt(notification.createdAt());
    }
}
