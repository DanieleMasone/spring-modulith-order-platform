package com.example.orderplatform.customers.api;

import java.util.UUID;

public interface CustomerDirectory {

    CustomerSnapshot getRequiredCustomer(UUID customerId);
}
