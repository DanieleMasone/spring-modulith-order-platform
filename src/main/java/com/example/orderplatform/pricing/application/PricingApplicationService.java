package com.example.orderplatform.pricing.application;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import com.example.orderplatform.ResourceNotFoundException;
import com.example.orderplatform.pricing.api.PricedLine;
import com.example.orderplatform.pricing.api.PricingQuote;
import com.example.orderplatform.pricing.api.PricingRequest;
import com.example.orderplatform.pricing.api.PricingService;
import com.example.orderplatform.pricing.domain.CatalogCurrencyPolicy;
import com.example.orderplatform.pricing.infrastructure.PriceCatalogItemEntity;
import com.example.orderplatform.pricing.infrastructure.PriceCatalogRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PricingApplicationService implements PricingService {

    private final PriceCatalogRepository catalog;

    public PricingApplicationService(PriceCatalogRepository catalog) {
        this.catalog = catalog;
    }

    @Override
    public PricingQuote quoteFor(List<PricingRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessRuleViolationException("At least one order item is required.");
        }

        List<PricedLine> lines = requests.stream()
                .map(this::price)
                .toList();
        CatalogCurrencyPolicy.requireSingleCurrency(lines.stream().map(PricedLine::lineTotal).toList());

        Money total = lines.stream()
                .map(PricedLine::lineTotal)
                .reduce((left, right) -> left.add(right))
                .orElseThrow();

        return new PricingQuote(lines, total);
    }

    private PricedLine price(PricingRequest request) {
        if (request.quantity() < 1) {
            throw new BusinessRuleViolationException("Quantity must be greater than zero.");
        }

        PriceCatalogItemEntity item = catalog.findById(request.productCode())
                .filter(PriceCatalogItemEntity::active)
                .orElseThrow(() -> new ResourceNotFoundException("Product " + request.productCode() + " was not found."));

        Money unitPrice = Money.of(item.unitAmount(), item.currency());
        return new PricedLine(request.productCode(), request.quantity(), unitPrice, unitPrice.multiply(request.quantity()));
    }
}
