package com.example.orderplatform;

/**
 * Raised when a request conflicts with an existing resource, such as a duplicate customer email.
 */
public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
