package com.example.orderplatform.pricing.api;

import com.example.orderplatform.generated.api.PricingApi;
import com.example.orderplatform.generated.model.Money;
import com.example.orderplatform.generated.model.PricingLineResponse;
import com.example.orderplatform.generated.model.PricingQuoteRequest;
import com.example.orderplatform.generated.model.PricingQuoteResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PricingRestController implements PricingApi {

    private final PricingService pricing;

    PricingRestController(PricingService pricing) {
        this.pricing = pricing;
    }

    @Override
    public ResponseEntity<PricingQuoteResponse> quotePrices(PricingQuoteRequest request) {
        PricingQuote quote = pricing.quoteFor(request.getItems().stream()
                .map(item -> new PricingRequest(item.getProductCode(), item.getQuantity()))
                .toList());

        return ResponseEntity.ok(new PricingQuoteResponse()
                .total(toMoney(quote.total()))
                .lines(toLines(quote.lines())));
    }

    private List<PricingLineResponse> toLines(List<PricedLine> lines) {
        return lines.stream()
                .map(line -> new PricingLineResponse()
                        .productCode(line.productCode())
                        .quantity(line.quantity())
                        .unitPrice(toMoney(line.unitPrice()))
                        .lineTotal(toMoney(line.lineTotal())))
                .toList();
    }

    private Money toMoney(com.example.orderplatform.Money money) {
        return new Money().amount(money.amount()).currency(money.currency());
    }
}
