package com.example.orderplatform.customers.application;

import com.example.orderplatform.ResourceNotFoundException;
import com.example.orderplatform.ResourceConflictException;
import com.example.orderplatform.customers.api.CustomerDirectory;
import com.example.orderplatform.customers.api.CustomerSnapshot;
import com.example.orderplatform.customers.domain.CustomerRegistration;
import com.example.orderplatform.customers.domain.CustomerStatus;
import com.example.orderplatform.customers.infrastructure.CustomerEntity;
import com.example.orderplatform.customers.infrastructure.CustomerRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerApplicationService implements CustomerDirectory {

    private final CustomerRepository customers;

    public CustomerApplicationService(CustomerRepository customers) {
        this.customers = customers;
    }

    public CustomerSnapshot createCustomer(String fullName, String email) {
        CustomerRegistration registration = CustomerRegistration.register(fullName, email);
        customers.findByEmail(registration.email()).ifPresent(existing -> {
            throw new ResourceConflictException("A customer with this email already exists.");
        });

        CustomerEntity customer = new CustomerEntity(
                UUID.randomUUID(),
                registration.email(),
                registration.fullName(),
                CustomerStatus.ACTIVE,
                OffsetDateTime.now());

        return toSnapshot(customers.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerSnapshot getRequiredCustomer(UUID customerId) {
        return customers.findById(customerId)
                .map(this::toSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + customerId + " was not found."));
    }

    private CustomerSnapshot toSnapshot(CustomerEntity customer) {
        return new CustomerSnapshot(customer.id(), customer.email(), customer.fullName(), customer.status().name());
    }
}
