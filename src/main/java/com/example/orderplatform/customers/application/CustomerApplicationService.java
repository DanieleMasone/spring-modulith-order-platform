package com.example.orderplatform.customers.application;

import com.example.orderplatform.BusinessRuleViolationException;
import com.example.orderplatform.ResourceNotFoundException;
import com.example.orderplatform.customers.api.CustomerDirectory;
import com.example.orderplatform.customers.api.CustomerSnapshot;
import com.example.orderplatform.customers.domain.CustomerStatus;
import com.example.orderplatform.customers.infrastructure.CustomerEntity;
import com.example.orderplatform.customers.infrastructure.CustomerRepository;
import java.time.OffsetDateTime;
import java.util.Locale;
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
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        customers.findByEmail(normalizedEmail).ifPresent(existing -> {
            throw new BusinessRuleViolationException("A customer with this email already exists.");
        });

        CustomerEntity customer = new CustomerEntity(
                UUID.randomUUID(),
                normalizedEmail,
                fullName.strip(),
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
