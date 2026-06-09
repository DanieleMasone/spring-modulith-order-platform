package com.example.orderplatform.orders.api;

import com.example.orderplatform.generated.api.OrdersApi;
import com.example.orderplatform.generated.model.Money;
import com.example.orderplatform.generated.model.OrderCreateRequest;
import com.example.orderplatform.generated.model.OrderLineResponse;
import com.example.orderplatform.generated.model.OrderResponse;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
class OrderRestController implements OrdersApi {

    private final OrderManagement orders;

    OrderRestController(OrderManagement orders) {
        this.orders = orders;
    }

    @Override
    public ResponseEntity<OrderResponse> createOrder(OrderCreateRequest request) {
        OrderSummary order = orders.placeOrder(new OrderCommand(
                request.getCustomerId(),
                request.getItems().stream()
                        .map(item -> new OrderItemCommand(item.getProductCode(), item.getQuantity()))
                        .toList()));

        return ResponseEntity
                .created(URI.create("/orders/" + order.id()))
                .body(toResponse(order));
    }

    @Override
    public ResponseEntity<OrderResponse> getOrder(UUID orderId) {
        return ResponseEntity.ok(toResponse(orders.getOrder(orderId)));
    }

    private OrderResponse toResponse(OrderSummary order) {
        return new OrderResponse()
                .id(order.id())
                .customerId(order.customerId())
                .status(OrderResponse.StatusEnum.valueOf(order.status()))
                .total(toMoney(order.total()))
                .lines(toLines(order.lines()))
                .createdAt(order.createdAt());
    }

    private List<OrderLineResponse> toLines(List<OrderLineSummary> lines) {
        return lines.stream()
                .map(line -> new OrderLineResponse()
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
