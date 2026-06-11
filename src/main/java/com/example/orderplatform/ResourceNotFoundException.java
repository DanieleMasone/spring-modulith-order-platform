package com.example.orderplatform;

/**
 * Raised when a required aggregate or lookup target cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
