package com.example.orderplatform.notifications.infrastructure;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findTop50ByOrderByCreatedAtDesc();

    @Modifying
    @Query(value = """
            INSERT INTO notifications (id, source_id, recipient, channel, type, payload, status, created_at)
            VALUES (:id, :sourceId, :recipient, :channel, :type, :payload, 'READY', :createdAt)
            ON CONFLICT (source_id, type) DO NOTHING
            """, nativeQuery = true)
    void insertIfAbsent(UUID id, UUID sourceId, String recipient, String channel,
            String type, String payload, OffsetDateTime createdAt);
}
