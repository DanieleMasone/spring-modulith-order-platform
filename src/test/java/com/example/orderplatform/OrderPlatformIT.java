package com.example.orderplatform;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.orderplatform.customers.application.CustomerApplicationService;
import com.example.orderplatform.notifications.api.NotificationLog;
import com.example.orderplatform.notifications.api.NotificationSummary;
import com.example.orderplatform.orders.api.OrderCommand;
import com.example.orderplatform.orders.api.OrderCreatedEvent;
import com.example.orderplatform.orders.api.OrderItemCommand;
import com.example.orderplatform.orders.api.OrderManagement;
import com.example.orderplatform.payments.api.PaymentAuthorizedEvent;
import com.example.orderplatform.payments.api.PaymentManagement;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@SpringBootTest
@RecordApplicationEvents
class OrderPlatformIT extends AbstractPostgresIntegrationTest {

    @Autowired
    CustomerApplicationService customers;

    @Autowired
    OrderManagement orders;

    @Autowired
    PaymentManagement payments;

    @Autowired
    NotificationLog notifications;

    @Autowired
    ApplicationEvents applicationEvents;

    @Test
    void createsOrderPreparesPaymentAndRecordsNotifications() {
        var customer = customers.createCustomer("Ada Lovelace", "ada.lovelace@example.com");

        var order = orders.placeOrder(new OrderCommand(customer.id(), List.of(
                new OrderItemCommand("SKU-COFFEE-MUG", 2),
                new OrderItemCommand("SKU-NOTEBOOK", 1))));

        assertThat(order.status()).isEqualTo("SUBMITTED");
        assertThat(order.total().amount()).isEqualByComparingTo(new BigDecimal("49.97"));
        assertThat(applicationEvents.stream(OrderCreatedEvent.class))
                .anySatisfy(event -> assertThat(event.orderId()).isEqualTo(order.id()));

        var authorizedPayment = payments.authorize(order.id(), order.total());
        assertThat(authorizedPayment.status()).isEqualTo("AUTHORIZED");
        assertThat(applicationEvents.stream(PaymentAuthorizedEvent.class))
                .anySatisfy(event -> assertThat(event.paymentId()).isEqualTo(authorizedPayment.id()));

        assertThat(notifications.listRecent())
                .extracting(NotificationSummary::type)
                .contains("ORDER_CREATED", "PAYMENT_AUTHORIZED");
    }
}
