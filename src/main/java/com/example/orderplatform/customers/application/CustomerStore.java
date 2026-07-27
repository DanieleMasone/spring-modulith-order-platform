package com.example.orderplatform.customers.application;

import com.example.orderplatform.customers.api.CustomerSnapshot;
import com.example.orderplatform.customers.domain.CustomerRegistration;
import com.example.orderplatform.customers.domain.CustomerStatus;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound persistence port for customer registration and lookup.
 */
public interface CustomerStore {

    /**
     * Finds a customer by its identifier.
     *
     * @param customerId customer identifier
     * @return customer snapshot when present
     */
    Optional<CustomerSnapshot> findById(UUID customerId);

    /**
     * Persists a new customer registration.
     *
     * @param id generated customer identifier
     * @param registration validated registration data
     * @param status initial customer status
     * @param createdAt creation timestamp
     * @return saved customer snapshot
     * @throws com.example.orderplatform.ResourceConflictException when the email is already registered
     */
    CustomerSnapshot saveNew(UUID id, CustomerRegistration registration, CustomerStatus status, OffsetDateTime createdAt);
}
