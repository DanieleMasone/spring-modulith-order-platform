package com.example.orderplatform.orders.application;

import com.example.orderplatform.orders.api.OrderSummary;
import com.example.orderplatform.orders.domain.OrderDraft;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for submitted orders.
 */
public interface OrderStore {

    /**
     * Persists a submitted order draft.
     *
     * @param id generated order identifier
     * @param draft validated order draft
     * @param createdAt creation timestamp
     * @return saved order summary
     */
    OrderSummary save(UUID id, OrderDraft draft, OffsetDateTime createdAt);

    /**
     * Finds an order by identifier.
     *
     * @param orderId order identifier
     * @return order summary when present
     */
    Optional<OrderSummary> findById(UUID orderId);
}
