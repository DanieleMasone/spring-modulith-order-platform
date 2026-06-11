package com.example.orderplatform.pricing.application;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.Money;
import com.example.orderplatform.ResourceNotFoundException;
import com.example.orderplatform.pricing.api.PricedLine;
import com.example.orderplatform.pricing.api.PricingQuote;
import com.example.orderplatform.pricing.api.PricingRequest;
import com.example.orderplatform.pricing.api.PricingService;
import com.example.orderplatform.pricing.domain.CatalogCurrencyPolicy;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that reads the active catalog and calculates quote totals for order placement.
 */
@Service
@Transactional(readOnly = true)
public class PricingApplicationService implements PricingService {

    private final PriceCatalog catalog;

    public PricingApplicationService(PriceCatalog catalog) {
        this.catalog = catalog;
    }

    /**
     * Quotes the requested products using active catalog entries.
     *
     * @param requests product and quantity requests
     * @return priced quote with one line per request
     * @throws BusinessRuleViolationException when the request is empty, has an invalid quantity or mixes currencies
     * @throws ResourceNotFoundException when a product code is not active in the catalog
     */
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

        CatalogPrice item = catalog.findActivePrice(request.productCode())
                .orElseThrow(() -> new ResourceNotFoundException("Product " + request.productCode() + " was not found."));

        return new PricedLine(
                item.productCode(),
                request.quantity(),
                item.unitPrice(),
                item.unitPrice().multiply(request.quantity()));
    }
}
