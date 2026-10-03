package com.example.orderplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.orderplatform.customers.application.CustomerApplicationService;
import com.example.orderplatform.orders.api.OrderCommand;
import com.example.orderplatform.orders.api.OrderCreatedEvent;
import com.example.orderplatform.orders.api.OrderItemCommand;
import com.example.orderplatform.orders.api.OrderManagement;
import com.example.orderplatform.orders.api.OrderSummary;
import com.example.orderplatform.payments.api.PaymentAuthorizedEvent;
import com.example.orderplatform.payments.api.PaymentManagement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

class EventPublicationIT extends AbstractPostgresIntegrationTest {

    @Test
    void recoversFailedPaymentPreparationAfterRestart() {
        UUID orderId;

        try (var context = startApplication()) {
            var jdbc = resetDatabase(context);
            rejectWrites(jdbc, "payments", "INSERT");
            try {
                orderId = createOrder(context).id();
                assertThat(count(jdbc, "payments")).isZero();
                assertThat(count(jdbc, "orders")).isEqualTo(1);
                assertThat(count(jdbc, "notifications")).isEqualTo(1);
                assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM event_publication WHERE status = 'FAILED'", Integer.class))
                        .isEqualTo(1);
            } finally {
                allowWrites(jdbc, "payments");
            }
        }

        try (var context = startApplication()) {
            var jdbc = context.getBean(JdbcTemplate.class);
            assertThat(count(jdbc, "payments")).isEqualTo(1);
            assertThat(count(jdbc, "notifications")).isEqualTo(1);
            var payment = context.getBean(PaymentManagement.class).authorize(
                    orderId, context.getBean(OrderManagement.class).getOrder(orderId).total());
            assertThat(payment.status()).isEqualTo("AUTHORIZED");
            assertThat(count(jdbc, "notifications")).isEqualTo(2);
            assertThat(count(jdbc, "event_publication")).isZero();
        }
    }

    @Test
    void restartReplayDoesNotDuplicateCommittedEffectsWhenCompletionWasLost() {
        UUID orderId;

        try (var context = startApplication()) {
            var jdbc = resetDatabase(context);
            // Fail only the registry acknowledgement, after listener transactions have committed.
            rejectWrites(jdbc, "event_publication", "DELETE");
            try {
                var order = createOrder(context);
                orderId = order.id();
                context.getBean(PaymentManagement.class).authorize(order.id(), order.total());
                assertThat(count(jdbc, "payments")).isEqualTo(1);
                assertThat(count(jdbc, "notifications")).isEqualTo(2);
                assertThat(count(jdbc, "event_publication")).isEqualTo(3);
            } finally {
                allowWrites(jdbc, "event_publication");
            }
        }

        try (var context = startApplication()) {
            var jdbc = context.getBean(JdbcTemplate.class);
            assertThat(count(jdbc, "payments")).isEqualTo(1);
            assertThat(count(jdbc, "notifications")).isEqualTo(2);
            assertThat(jdbc.queryForObject(
                    "SELECT status FROM payments WHERE order_id = ?", String.class, orderId))
                    .isEqualTo("AUTHORIZED");
            assertThat(count(jdbc, "event_publication")).isZero();
        }
    }

    @Test
    void completesNormalPublicationsAndToleratesConcurrentDuplicateEvents() throws Exception {
        try (var context = startApplication()) {
            var jdbc = resetDatabase(context);
            var order = createOrder(context);
            assertThat(count(jdbc, "payments")).isEqualTo(1);
            assertThat(count(jdbc, "notifications")).isEqualTo(1);
            assertThat(count(jdbc, "event_publication")).isZero();

            var payment = context.getBean(PaymentManagement.class).authorize(order.id(), order.total());
            var orderEvent = new OrderCreatedEvent(order.id(), order.customerId(), order.total().amount(),
                    order.total().currency(), order.createdAt());
            var paymentEvent = new PaymentAuthorizedEvent(payment.id(), payment.orderId(), payment.amount().amount(),
                    payment.amount().currency(), payment.authorizedAt());
            var barrier = new CyclicBarrier(2);

            try (var executor = Executors.newFixedThreadPool(2)) {
                var tasks = executor.invokeAll(List.of(() -> {
                    barrier.await(30, TimeUnit.SECONDS);
                    publish(context, orderEvent);
                    publish(context, paymentEvent);
                    return true;
                }, () -> {
                    barrier.await(30, TimeUnit.SECONDS);
                    publish(context, orderEvent);
                    publish(context, paymentEvent);
                    return true;
                }));
                for (var task : tasks) {
                    assertThat(task.get(30, TimeUnit.SECONDS)).isEqualTo(true);
                }
            }

            assertThat(count(jdbc, "payments")).isEqualTo(1);
            assertThat(count(jdbc, "notifications")).isEqualTo(2);
        }

        // Concurrent identical publications can leave an acknowledgement outstanding.
        try (var context = startApplication()) {
            var jdbc = context.getBean(JdbcTemplate.class);
            assertThat(count(jdbc, "payments")).isEqualTo(1);
            assertThat(count(jdbc, "notifications")).isEqualTo(2);
            assertThat(count(jdbc, "event_publication")).isZero();
        }
    }

    @Test
    void rollsBackPublicationsTogetherWithOrderTransaction() {
        try (var context = startApplication()) {
            var jdbc = resetDatabase(context);
            var transaction = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));

            assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
                createOrder(context);
                assertThat(count(jdbc, "event_publication")).isEqualTo(2);
                throw new IllegalStateException("abort order transaction");
            })).isInstanceOf(IllegalStateException.class).hasMessage("abort order transaction");

            assertThat(count(jdbc, "orders")).isZero();
            assertThat(count(jdbc, "payments")).isZero();
            assertThat(count(jdbc, "notifications")).isZero();
            assertThat(count(jdbc, "event_publication")).isZero();
        }
    }

    private ConfigurableApplicationContext startApplication() {
        return new SpringApplicationBuilder(OrderPlatformApplication.class)
                .web(WebApplicationType.NONE)
                .run("--spring.datasource.url=" + postgres.getJdbcUrl(),
                        "--spring.datasource.username=" + postgres.getUsername(),
                        "--spring.datasource.password=" + postgres.getPassword(),
                        "--spring.main.banner-mode=off");
    }

    private JdbcTemplate resetDatabase(ConfigurableApplicationContext context) {
        var jdbc = context.getBean(JdbcTemplate.class);
        jdbc.execute("TRUNCATE event_publication, notifications, payments, orders, customers CASCADE");
        return jdbc;
    }

    private OrderSummary createOrder(ConfigurableApplicationContext context) {
        var customer = context.getBean(CustomerApplicationService.class)
                .createCustomer("Ada Lovelace", "ada-" + UUID.randomUUID() + "@example.com");
        return context.getBean(OrderManagement.class).placeOrder(new OrderCommand(customer.id(),
                List.of(new OrderItemCommand("SKU-COFFEE-MUG", 2))));
    }

    private void publish(ConfigurableApplicationContext context, Object event) {
        new TransactionTemplate(context.getBean(PlatformTransactionManager.class))
                .executeWithoutResult(status -> context.publishEvent(event));
    }

    private int count(JdbcTemplate jdbc, String table) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class);
    }

    private void rejectWrites(JdbcTemplate jdbc, String table, String operation) {
        jdbc.execute("""
                CREATE FUNCTION reject_event_write() RETURNS trigger LANGUAGE plpgsql AS $$
                BEGIN RAISE EXCEPTION 'simulated persistence failure'; END $$
                """);
        jdbc.execute("CREATE TRIGGER reject_event_write BEFORE " + operation + " ON " + table
                + " FOR EACH ROW EXECUTE FUNCTION reject_event_write()");
    }

    private void allowWrites(JdbcTemplate jdbc, String table) {
        jdbc.execute("DROP TRIGGER reject_event_write ON " + table);
        jdbc.execute("DROP FUNCTION reject_event_write()");
    }
}
