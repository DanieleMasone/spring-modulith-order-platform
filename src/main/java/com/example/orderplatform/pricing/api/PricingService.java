package com.example.orderplatform.pricing.api;

import java.util.List;

public interface PricingService {

    PricingQuote quoteFor(List<PricingRequest> requests);
}
