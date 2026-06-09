package com.example.orderplatform.orders.api;

public record OrderItemCommand(String productCode, int quantity) {
}
