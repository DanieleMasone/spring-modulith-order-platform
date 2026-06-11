/**
 * Notification module that records notification intents created from order and payment events.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = { "orders :: api", "payments :: api" }
)
package com.example.orderplatform.notifications;
