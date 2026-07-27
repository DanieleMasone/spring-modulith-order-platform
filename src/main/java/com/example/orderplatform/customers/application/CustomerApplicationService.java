package com.example.orderplatform.customers.application;

import com.example.orderplatform.ResourceConflictException;
import com.example.orderplatform.ResourceNotFoundException;
import com.example.orderplatform.customers.api.CustomerDirectory;
import com.example.orderplatform.customers.api.CustomerSnapshot;
import com.example.orderplatform.customers.domain.CustomerRegistration;
import com.example.orderplatform.customers.domain.CustomerStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that registers customers and backs the customer lookup module API.
 */
@Service
@Transactional
public class CustomerApplicationService implements CustomerDirectory {

    private final CustomerStore customers;

    public CustomerApplicationService(CustomerStore customers) {
        this.customers = customers;
    }

    /**
     * Registers a new active customer after normalizing and validating the submitted identity fields.
     *
     * @param fullName customer display name
     * @param email customer email address
     * @return created customer snapshot
     * @throws ResourceConflictException when another customer already uses the email address
     */
    public CustomerSnapshot createCustomer(String fullName, String email) {
        CustomerRegistration registration = CustomerRegistration.register(fullName, email);
        return customers.saveNew(
                UUID.randomUUID(),
                registration,
                CustomerStatus.ACTIVE,
                OffsetDateTime.now());
    }

    /**
     * Retrieves the customer snapshot required by order placement.
     *
     * @param customerId customer identifier
     * @return immutable customer snapshot
     * @throws ResourceNotFoundException when the customer does not exist
     */
    @Override
    @Transactional(readOnly = true)
    public CustomerSnapshot getRequiredCustomer(UUID customerId) {
        return customers.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer " + customerId + " was not found."));
    }
}
