package com.example.orderplatform.pricing.api;

import java.util.List;

/**
 * Module API for quoting product requests against the price catalog.
 */
public interface PricingService {

    /**
     * Creates a deterministic quote for the requested product quantities.
     *
     * @param requests product and quantity requests
     * @return quoted lines and aggregate total
     * @throws com.example.orderplatform.BusinessRuleViolationException when the request is empty, has
     * invalid quantities or mixes currencies
     * @throws com.example.orderplatform.ResourceNotFoundException when a requested product is not active
     * in the catalog
     */
    PricingQuote quoteFor(List<PricingRequest> requests);
}
