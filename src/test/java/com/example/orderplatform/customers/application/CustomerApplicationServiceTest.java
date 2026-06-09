package com.example.orderplatform.customers.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.orderplatform.ResourceConflictException;
import com.example.orderplatform.customers.domain.CustomerStatus;
import com.example.orderplatform.customers.infrastructure.CustomerEntity;
import com.example.orderplatform.customers.infrastructure.CustomerRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerApplicationServiceTest {

    @Mock
    CustomerRepository customers;

    @Test
    void createsActiveCustomerThroughModuleApi() {
        when(customers.findByEmail("ada@example.com")).thenReturn(Optional.empty());
        when(customers.save(any(CustomerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var service = new CustomerApplicationService(customers);
        var customer = service.createCustomer(" Ada Lovelace ", " ADA@EXAMPLE.COM ");

        assertThat(customer.email()).isEqualTo("ada@example.com");
        assertThat(customer.fullName()).isEqualTo("Ada Lovelace");
        assertThat(customer.status()).isEqualTo("ACTIVE");

        ArgumentCaptor<CustomerEntity> savedCustomer = ArgumentCaptor.forClass(CustomerEntity.class);
        verify(customers).save(savedCustomer.capture());
        assertThat(savedCustomer.getValue().status()).isEqualTo(CustomerStatus.ACTIVE);
    }

    @Test
    void rejectsDuplicateEmailThroughConflictException() {
        var existing = new CustomerEntity(
                UUID.randomUUID(),
                "ada@example.com",
                "Ada Lovelace",
                CustomerStatus.ACTIVE,
                OffsetDateTime.now());
        when(customers.findByEmail("ada@example.com")).thenReturn(Optional.of(existing));

        var service = new CustomerApplicationService(customers);

        assertThatThrownBy(() -> service.createCustomer("Ada Lovelace", "ADA@EXAMPLE.COM"))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("already exists");
    }
}
