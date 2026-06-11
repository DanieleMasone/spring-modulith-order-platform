package com.example.orderplatform.notifications.application;

import com.example.orderplatform.notifications.api.NotificationSummary;
import com.example.orderplatform.notifications.domain.NotificationDraft;
import java.util.List;

/**
 * Outbound persistence port for recorded notification intents.
 */
public interface NotificationStore {

    /**
     * Persists a ready notification intent.
     *
     * @param draft validated notification draft
     */
    void save(NotificationDraft draft);

    /**
     * Lists recent notification records.
     *
     * @return recent notifications in reverse creation order
     */
    List<NotificationSummary> findRecent();
}
