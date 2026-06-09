package com.example.orderplatform.customers.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.orderplatform.BusinessRuleViolationException;
import org.junit.jupiter.api.Test;

class CustomerRegistrationTest {

    @Test
    void normalizesCustomerIdentity() {
        var registration = CustomerRegistration.register("  Ada Lovelace  ", " ADA.LOVELACE@EXAMPLE.COM ");

        assertThat(registration.fullName()).isEqualTo("Ada Lovelace");
        assertThat(registration.email()).isEqualTo("ada.lovelace@example.com");
    }

    @Test
    void rejectsInvalidEmailAddresses() {
        assertThatThrownBy(() -> CustomerRegistration.register("Ada Lovelace", "ada"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("valid email");
    }

    @Test
    void rejectsTooShortNames() {
        assertThatThrownBy(() -> CustomerRegistration.register("A", "ada@example.com"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("between 2 and 160");
    }
}
