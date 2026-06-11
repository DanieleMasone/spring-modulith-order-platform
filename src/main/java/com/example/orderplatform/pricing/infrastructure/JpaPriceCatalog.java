package com.example.orderplatform.pricing.infrastructure;

import com.example.orderplatform.Money;
import com.example.orderplatform.pricing.application.CatalogPrice;
import com.example.orderplatform.pricing.application.PriceCatalog;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
class JpaPriceCatalog implements PriceCatalog {

    private final PriceCatalogRepository catalog;

    JpaPriceCatalog(PriceCatalogRepository catalog) {
        this.catalog = catalog;
    }

    @Override
    public Optional<CatalogPrice> findActivePrice(String productCode) {
        return catalog.findById(productCode)
                .filter(PriceCatalogItemEntity::active)
                .map(item -> new CatalogPrice(item.productCode(), Money.of(item.unitAmount(), item.currency())));
    }
}
