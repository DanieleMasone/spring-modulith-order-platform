package com.example.orderplatform.customers.api;

import java.util.UUID;

/**
 * Module API for resolving customers required by other modules.
 */
public interface CustomerDirectory {

    /**
     * Retrieves a customer by identifier.
     *
     * @param customerId customer identifier
     * @return immutable customer snapshot
     * @throws com.example.orderplatform.ResourceNotFoundException when the customer does not exist
     */
    CustomerSnapshot getRequiredCustomer(UUID customerId);
}
