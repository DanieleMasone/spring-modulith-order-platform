package com.example.orderplatform.customers.domain;

import com.example.orderplatform.BusinessRuleViolationException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validated registration command for creating a customer.
 *
 * <p>The email address is stripped and lower-cased before persistence so uniqueness checks are
 * deterministic.
 *
 * @param fullName normalized customer display name
 * @param email normalized email address
 */
public record CustomerRegistration(String fullName, String email) {

    private static final Pattern BASIC_EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public CustomerRegistration {
        if (fullName == null || fullName.isBlank()) {
            throw new BusinessRuleViolationException("Customer full name is required.");
        }
        if (email == null || email.isBlank()) {
            throw new BusinessRuleViolationException("Customer email is required.");
        }

        fullName = fullName.strip();
        email = email.strip().toLowerCase(Locale.ROOT);

        if (fullName.length() < 2 || fullName.length() > 160) {
            throw new BusinessRuleViolationException("Customer full name must contain between 2 and 160 characters.");
        }
        if (email.length() > 320 || !BASIC_EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessRuleViolationException("Customer email must be a valid email address.");
        }
    }

    /**
     * Creates a validated registration value.
     *
     * @param fullName submitted customer display name
     * @param email submitted email address
     * @return normalized registration
     */
    public static CustomerRegistration register(String fullName, String email) {
        return new CustomerRegistration(fullName, email);
    }
}
