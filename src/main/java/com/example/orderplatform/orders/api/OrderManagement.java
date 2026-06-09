package com.example.orderplatform.orders.api;

import java.util.UUID;

public interface OrderManagement {

    OrderSummary placeOrder(OrderCommand command);

    OrderSummary getOrder(UUID orderId);
}
