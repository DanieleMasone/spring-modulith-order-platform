package com.example.orderplatform.customers.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.orderplatform.customers.api.CustomerSnapshot;
import com.example.orderplatform.customers.domain.CustomerRegistration;
import com.example.orderplatform.customers.domain.CustomerStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerApplicationServiceTest {

    @Mock
    CustomerStore customers;

    @Test
    void createsActiveCustomerThroughModuleApi() {
        when(customers.saveNew(any(UUID.class), any(CustomerRegistration.class), eq(CustomerStatus.ACTIVE), any(OffsetDateTime.class)))
                .thenAnswer(invocation -> {
                    CustomerRegistration registration = invocation.getArgument(1);
                    return new CustomerSnapshot(
                            invocation.getArgument(0),
                            registration.email(),
                            registration.fullName(),
                            invocation.<CustomerStatus>getArgument(2).name());
                });

        var service = new CustomerApplicationService(customers);
        var customer = service.createCustomer(" Ada Lovelace ", " ADA@EXAMPLE.COM ");

        assertThat(customer.email()).isEqualTo("ada@example.com");
        assertThat(customer.fullName()).isEqualTo("Ada Lovelace");
        assertThat(customer.status()).isEqualTo("ACTIVE");

        ArgumentCaptor<CustomerRegistration> savedRegistration = ArgumentCaptor.forClass(CustomerRegistration.class);
        verify(customers).saveNew(any(UUID.class), savedRegistration.capture(), eq(CustomerStatus.ACTIVE), any(OffsetDateTime.class));
        assertThat(savedRegistration.getValue().email()).isEqualTo("ada@example.com");
    }
}
