package com.example.orderplatform.notifications.application;

import com.example.orderplatform.notifications.api.NotificationSummary;
import com.example.orderplatform.notifications.domain.NotificationDraft;
import java.util.List;
import java.util.UUID;

/**
 * Outbound persistence port for recorded notification intents.
 */
public interface NotificationStore {

    /**
     * Persists at most one ready notification intent per source and notification type.
     *
     * @param sourceId order or payment identifier that caused the notification
     * @param draft validated notification draft
     */
    void save(UUID sourceId, NotificationDraft draft);

    /**
     * Lists recent notification records.
     *
     * @return recent notifications in reverse creation order
     */
    List<NotificationSummary> findRecent();
}
