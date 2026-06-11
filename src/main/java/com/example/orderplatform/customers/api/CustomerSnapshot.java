package com.example.orderplatform.customers.api;

import java.util.UUID;

/**
 * Immutable customer view exposed by the customer module API.
 *
 * @param id customer identifier
 * @param email normalized email address
 * @param fullName display name supplied at registration
 * @param status current customer status
 */
public record CustomerSnapshot(UUID id, String email, String fullName, String status) {
}
