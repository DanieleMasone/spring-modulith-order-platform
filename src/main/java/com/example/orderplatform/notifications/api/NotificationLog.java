package com.example.orderplatform.notifications.api;

import java.util.List;

/**
 * Module API for reading notification intents recorded by the notification module.
 */
public interface NotificationLog {

    /**
     * Lists the most recent notification intents.
     *
     * @return notification summaries in reverse creation order
     */
    List<NotificationSummary> listRecent();
}
