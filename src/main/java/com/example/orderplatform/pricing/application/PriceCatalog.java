package com.example.orderplatform.pricing.application;

import java.util.Optional;

/**
 * Outbound catalog port used by pricing use cases.
 */
public interface PriceCatalog {

    /**
     * Finds an active catalog price for a product code.
     *
     * @param productCode requested product code
     * @return active catalog price when the product can be quoted
     */
    Optional<CatalogPrice> findActivePrice(String productCode);
}
