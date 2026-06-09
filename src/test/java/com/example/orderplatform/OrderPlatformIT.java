package com.example.orderplatform;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.orderplatform.customers.application.CustomerApplicationService;
import com.example.orderplatform.notifications.api.NotificationLog;
import com.example.orderplatform.notifications.api.NotificationSummary;
import com.example.orderplatform.orders.api.OrderCommand;
import com.example.orderplatform.orders.api.OrderItemCommand;
import com.example.orderplatform.orders.api.OrderManagement;
import com.example.orderplatform.payments.api.PaymentManagement;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class OrderPlatformIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    CustomerApplicationService customers;

    @Autowired
    OrderManagement orders;

    @Autowired
    PaymentManagement payments;

    @Autowired
    NotificationLog notifications;

    @Test
    void createsOrderPreparesPaymentAndRecordsNotifications() {
        var customer = customers.createCustomer("Ada Lovelace", "ada.lovelace@example.com");

        var order = orders.placeOrder(new OrderCommand(customer.id(), List.of(
                new OrderItemCommand("SKU-COFFEE-MUG", 2),
                new OrderItemCommand("SKU-NOTEBOOK", 1))));

        assertThat(order.status()).isEqualTo("SUBMITTED");
        assertThat(order.total().amount()).isEqualByComparingTo(new BigDecimal("49.97"));

        var pendingPayment = payments.findByOrderId(order.id()).orElseThrow();
        assertThat(pendingPayment.status()).isEqualTo("PENDING");

        var authorizedPayment = payments.authorize(order.id(), order.total());
        assertThat(authorizedPayment.status()).isEqualTo("AUTHORIZED");

        assertThat(notifications.listRecent())
                .extracting(NotificationSummary::type)
                .contains("ORDER_CREATED", "PAYMENT_AUTHORIZED");
    }
}
