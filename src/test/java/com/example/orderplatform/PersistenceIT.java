package com.example.orderplatform;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.orderplatform.customers.domain.CustomerStatus;
import com.example.orderplatform.customers.infrastructure.CustomerEntity;
import com.example.orderplatform.customers.infrastructure.CustomerRepository;
import com.example.orderplatform.pricing.infrastructure.PriceCatalogRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PersistenceIT extends AbstractPostgresIntegrationTest {

    @Autowired
    PriceCatalogRepository catalog;

    @Autowired
    CustomerRepository customers;

    @Test
    void flywaySeedsReferenceCatalogForPricingUseCases() {
        var item = catalog.findById("SKU-COFFEE-MUG").orElseThrow();

        assertThat(item.productCode()).isEqualTo("SKU-COFFEE-MUG");
        assertThat(item.currency()).isEqualTo("EUR");
        assertThat(item.active()).isTrue();
        assertThat(item.unitAmount()).isPositive();
    }

    @Test
    void customerRepositoryPersistsAndFindsCustomersByEmail() {
        String email = "customer-" + UUID.randomUUID() + "@example.com";
        var customer = new CustomerEntity(
                UUID.randomUUID(),
                email,
                "Repository Customer",
                CustomerStatus.ACTIVE,
                OffsetDateTime.now());

        customers.saveAndFlush(customer);

        assertThat(customers.findByEmail(email))
                .isPresent()
                .get()
                .extracting(CustomerEntity::fullName)
                .isEqualTo("Repository Customer");
    }
}
