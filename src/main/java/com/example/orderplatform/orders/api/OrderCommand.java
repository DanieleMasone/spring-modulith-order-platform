package com.example.orderplatform.orders.api;

import java.util.List;
import java.util.UUID;

public record OrderCommand(UUID customerId, List<OrderItemCommand> items) {
}
