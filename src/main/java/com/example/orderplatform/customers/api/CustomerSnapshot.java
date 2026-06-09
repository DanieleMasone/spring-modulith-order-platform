package com.example.orderplatform.customers.api;

import java.util.UUID;

public record CustomerSnapshot(UUID id, String email, String fullName, String status) {
}
