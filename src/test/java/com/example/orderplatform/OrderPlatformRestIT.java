package com.example.orderplatform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
class OrderPlatformRestIT extends AbstractPostgresIntegrationTest {

    @Autowired
    MockMvc mockMvc;

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
    void returnsProblemDetailsForRequestValidationFailures() throws Exception {
        assertValidationProblem(post("/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "email": "not-an-email",
                          "fullName": "A"
                        }
                        """));

        assertValidationProblem(post("/pricing/quote")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "items": [] }
                        """));

        assertValidationProblem(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "customerId": "%s",
                          "items": []
                        }
                        """.formatted(UUID.randomUUID())));

        assertValidationProblem(post("/payments/authorize")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "orderId": "%s",
                          "amount": { "amount": -1.00, "currency": "EU1" }
                        }
                        """.formatted(UUID.randomUUID())));
    }

    @Test
    void returnsProblemDetailsForMalformedRequests() throws Exception {
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://example.com/problems/malformed-request"))
                .andExpect(jsonPath("$.title").value("Malformed request"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsProblemDetailsForInvalidLookupIdentifiers() throws Exception {
        for (String path : List.of(
                "/customers/not-a-uuid",
                "/orders/not-a-uuid",
                "/payments/not-a-uuid")) {
            mockMvc.perform(get(path))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.title").value("Validation failed"))
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Test
    void returnsProblemDetailsForLookupNotFoundFailures() throws Exception {
        String missingId = UUID.randomUUID().toString();

        for (String path : List.of(
                "/customers/" + missingId,
                "/orders/" + missingId,
                "/payments/" + missingId)) {
            assertNotFoundProblem(get(path));
        }
    }

    @Test
    void returnsProblemDetailsForMissingProducts() throws Exception {
        assertNotFoundProblem(post("/pricing/quote")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "items": [
                            { "productCode": "SKU-MISSING", "quantity": 1 }
                          ]
                        }
                        """));
    }

    @Test
    void returnsProblemDetailsForMissingCommandResources() throws Exception {
        assertNotFoundProblem(post("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "customerId": "%s",
                          "items": [
                            { "productCode": "SKU-COFFEE-MUG", "quantity": 1 }
                          ]
                        }
                        """.formatted(UUID.randomUUID())));

        assertNotFoundProblem(post("/payments/authorize")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "orderId": "%s",
                          "amount": { "amount": 14.99, "currency": "EUR" }
                        }
                        """.formatted(UUID.randomUUID())));
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

        return new CustomerPayload(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
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

        return new OrderPayload(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
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

        return new PaymentPayload(JsonPath.read(result.getResponse().getContentAsString(), "$.id"));
    }

    private void assertValidationProblem(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://example.com/problems/validation-failed"))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    private void assertNotFoundProblem(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("https://example.com/problems/resource-not-found"))
                .andExpect(jsonPath("$.title").value("Resource not found"))
                .andExpect(jsonPath("$.status").value(404));
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
