package com.example.orderplatform;

/**
 * Raised when a request would violate a business invariant enforced by the domain model or use case.
 */
public class BusinessRuleViolationException extends RuntimeException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
