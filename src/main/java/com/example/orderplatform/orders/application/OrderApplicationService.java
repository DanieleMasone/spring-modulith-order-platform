package com.example.orderplatform.orders.application;

import com.example.orderplatform.ResourceNotFoundException;
import com.example.orderplatform.customers.api.CustomerDirectory;
import com.example.orderplatform.orders.api.OrderCommand;
import com.example.orderplatform.orders.api.OrderCreatedEvent;
import com.example.orderplatform.orders.api.OrderManagement;
import com.example.orderplatform.orders.api.OrderSummary;
import com.example.orderplatform.orders.domain.OrderDraft;
import com.example.orderplatform.orders.domain.OrderLineDraft;
import com.example.orderplatform.pricing.api.PricedLine;
import com.example.orderplatform.pricing.api.PricingRequest;
import com.example.orderplatform.pricing.api.PricingService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that coordinates customer lookup, pricing, order persistence and order events.
 */
@Service
@Transactional
public class OrderApplicationService implements OrderManagement {

    private final CustomerDirectory customers;
    private final PricingService pricing;
    private final OrderStore orders;
    private final ApplicationEventPublisher events;

    public OrderApplicationService(
            CustomerDirectory customers,
            PricingService pricing,
            OrderStore orders,
            ApplicationEventPublisher events) {
        this.customers = customers;
        this.pricing = pricing;
        this.orders = orders;
        this.events = events;
    }

    /**
     * Places a submitted order and publishes an order-created event after persistence.
     *
     * @param command order placement command
     * @return submitted order summary
     */
    @Override
    public OrderSummary placeOrder(OrderCommand command) {
        customers.getRequiredCustomer(command.customerId());

        List<PricingRequest> pricingRequests = command.items().stream()
                .map(item -> new PricingRequest(item.productCode(), item.quantity()))
                .toList();

        List<PricedLine> pricedLines = pricing.quoteFor(pricingRequests).lines();
        OrderDraft draft = new OrderDraft(
                command.customerId(),
                pricedLines.stream()
                        .map(line -> new OrderLineDraft(line.productCode(), line.quantity(), line.unitPrice()))
                        .toList());

        OrderSummary summary = orders.save(UUID.randomUUID(), draft, OffsetDateTime.now());

        events.publishEvent(new OrderCreatedEvent(
                summary.id(),
                summary.customerId(),
                summary.total().amount(),
                summary.total().currency(),
                summary.createdAt()));

        return summary;
    }

    /**
     * Retrieves a persisted order summary.
     *
     * @param orderId order identifier
     * @return submitted order summary
     * @throws ResourceNotFoundException when the order does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public OrderSummary getOrder(UUID orderId) {
        return orders.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " was not found."));
    }
}
