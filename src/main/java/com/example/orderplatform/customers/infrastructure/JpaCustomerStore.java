package com.example.orderplatform.customers.infrastructure;

import com.example.orderplatform.customers.api.CustomerSnapshot;
import com.example.orderplatform.customers.application.CustomerStore;
import com.example.orderplatform.customers.domain.CustomerRegistration;
import com.example.orderplatform.customers.domain.CustomerStatus;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class JpaCustomerStore implements CustomerStore {

    private final CustomerRepository customers;

    JpaCustomerStore(CustomerRepository customers) {
        this.customers = customers;
    }

    @Override
    public Optional<CustomerSnapshot> findById(UUID customerId) {
        return customers.findById(customerId).map(this::toSnapshot);
    }

    @Override
    public Optional<CustomerSnapshot> findByEmail(String email) {
        return customers.findByEmail(email).map(this::toSnapshot);
    }

    @Override
    public CustomerSnapshot saveNew(
            UUID id,
            CustomerRegistration registration,
            CustomerStatus status,
            OffsetDateTime createdAt) {
        CustomerEntity customer = new CustomerEntity(
                id,
                registration.email(),
                registration.fullName(),
                status,
                createdAt);

        return toSnapshot(customers.save(customer));
    }

    private CustomerSnapshot toSnapshot(CustomerEntity customer) {
        return new CustomerSnapshot(customer.id(), customer.email(), customer.fullName(), customer.status().name());
    }
}
