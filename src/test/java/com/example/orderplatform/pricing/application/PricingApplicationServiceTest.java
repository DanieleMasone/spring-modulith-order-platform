package com.example.orderplatform.pricing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.ResourceNotFoundException;
import com.example.orderplatform.pricing.api.PricingRequest;
import com.example.orderplatform.pricing.infrastructure.PriceCatalogItemEntity;
import com.example.orderplatform.pricing.infrastructure.PriceCatalogRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PricingApplicationServiceTest {

    @Mock
    PriceCatalogRepository catalog;

    @Test
    void calculatesQuoteFromActiveCatalogItems() {
        when(catalog.findById("SKU-COFFEE-MUG"))
                .thenReturn(Optional.of(catalogItem("SKU-COFFEE-MUG", "14.99", true)));
        when(catalog.findById("SKU-NOTEBOOK"))
                .thenReturn(Optional.of(catalogItem("SKU-NOTEBOOK", "19.99", true)));

        var service = new PricingApplicationService(catalog);
        var quote = service.quoteFor(List.of(
                new PricingRequest("SKU-COFFEE-MUG", 2),
                new PricingRequest("SKU-NOTEBOOK", 1)));

        assertThat(quote.total().amount()).isEqualByComparingTo(new BigDecimal("49.97"));
        assertThat(quote.lines()).hasSize(2);
        assertThat(quote.lines().getFirst().lineTotal().amount()).isEqualByComparingTo(new BigDecimal("29.98"));
    }

    @Test
    void rejectsEmptyPricingRequests() {
        var service = new PricingApplicationService(catalog);

        assertThatThrownBy(() -> service.quoteFor(List.of()))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("At least one order item");
    }

    @Test
    void rejectsMissingProducts() {
        when(catalog.findById("SKU-MISSING")).thenReturn(Optional.empty());

        var service = new PricingApplicationService(catalog);

        assertThatThrownBy(() -> service.quoteFor(List.of(new PricingRequest("SKU-MISSING", 1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SKU-MISSING");
    }

    @Test
    void rejectsInactiveProducts() {
        when(catalog.findById("SKU-INACTIVE"))
                .thenReturn(Optional.of(catalogItem("SKU-INACTIVE", "12.00", false)));

        var service = new PricingApplicationService(catalog);

        assertThatThrownBy(() -> service.quoteFor(List.of(new PricingRequest("SKU-INACTIVE", 1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("SKU-INACTIVE");
    }

    private PriceCatalogItemEntity catalogItem(String productCode, String unitAmount, boolean active) {
        return new PriceCatalogItemEntity(
                productCode,
                productCode,
                new BigDecimal(unitAmount),
                "EUR",
                active,
                OffsetDateTime.now());
    }
}
