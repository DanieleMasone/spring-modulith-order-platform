package com.example.orderplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderPlatformRestIT extends AbstractPostgresIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void declaredEndpointsWorkEndToEnd() throws Exception {
        String customerId = createCustomer("Grace Hopper", uniqueEmail()).id();

        mockMvc.perform(get("/customers/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.fullName").value("Grace Hopper"));

        mockMvc.perform(post("/pricing/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    { "productCode": "SKU-COFFEE-MUG", "quantity": 2 }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total.amount").value(29.98))
                .andExpect(jsonPath("$.total.currency").value("EUR"));

        var order = createOrder(customerId);
        String orderId = order.id();

        mockMvc.perform(get("/orders/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.total.amount").value(49.97));

        var payment = authorizePayment(orderId, "49.97");

        mockMvc.perform(get("/payments/{paymentId}", payment.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(payment.id()))
                .andExpect(jsonPath("$.status").value("AUTHORIZED"));

        String notificationsJson = mockMvc.perform(get("/notifications"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(notificationsJson).contains("ORDER_CREATED", "PAYMENT_AUTHORIZED");
    }

    @Test
    void returnsProblemDetailsForValidationFailures() throws Exception {
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "fullName": "A"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://example.com/problems/validation-failed"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsProblemDetailsForNotFoundFailures() throws Exception {
        mockMvc.perform(get("/orders/{orderId}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://example.com/problems/resource-not-found"))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnsProblemDetailsForMissingProducts() throws Exception {
        mockMvc.perform(post("/pricing/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "items": [
                                    { "productCode": "SKU-MISSING", "quantity": 1 }
                                  ]
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void returnsProblemDetailsForBusinessRuleFailures() throws Exception {
        String customerId = createCustomer("Margaret Hamilton", uniqueEmail()).id();
        String orderId = createOrder(customerId).id();

        mockMvc.perform(post("/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "%s",
                                  "amount": { "amount": 1.00, "currency": "EUR" }
                                }
                                """.formatted(orderId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Business rule violation"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsProblemDetailsForConflictFailures() throws Exception {
        String email = uniqueEmail();
        createCustomer("Katherine Johnson", email);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "fullName": "Katherine Johnson"
                                }
                                """.formatted(email)))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.status").value(409));
    }

    private CustomerPayload createCustomer(String fullName, String email) throws Exception {
        var result = mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "fullName": "%s"
                                }
                                """.formatted(email, fullName)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName").value(fullName))
                .andReturn();

        var body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new CustomerPayload(body.path("id").asText());
    }

    private OrderPayload createOrder(String customerId) throws Exception {
        var result = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "%s",
                                  "items": [
                                    { "productCode": "SKU-COFFEE-MUG", "quantity": 2 },
                                    { "productCode": "SKU-NOTEBOOK", "quantity": 1 }
                                  ]
                                }
                                """.formatted(customerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.total.amount").value(49.97))
                .andReturn();

        var body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new OrderPayload(body.path("id").asText());
    }

    private PaymentPayload authorizePayment(String orderId, String amount) throws Exception {
        var result = mockMvc.perform(post("/payments/authorize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderId": "%s",
                                  "amount": { "amount": %s, "currency": "EUR" }
                                }
                                """.formatted(orderId, amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andReturn();

        var body = objectMapper.readTree(result.getResponse().getContentAsString());
        return new PaymentPayload(body.path("id").asText());
    }

    private String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }

    private record CustomerPayload(String id) {
    }

    private record OrderPayload(String id) {
    }

    private record PaymentPayload(String id) {
    }
}
