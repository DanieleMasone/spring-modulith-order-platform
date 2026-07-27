package com.example.orderplatform;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.orderplatform.pricing.infrastructure.PriceCatalogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PersistenceIT extends AbstractPostgresIntegrationTest {

    @Autowired
    PriceCatalogRepository catalog;

    @Test
    void flywaySeedsReferenceCatalogForPricingUseCases() {
        var item = catalog.findById("SKU-COFFEE-MUG").orElseThrow();

        assertThat(item.productCode()).isEqualTo("SKU-COFFEE-MUG");
        assertThat(item.currency()).isEqualTo("EUR");
        assertThat(item.active()).isTrue();
        assertThat(item.unitAmount()).isPositive();
    }
}
