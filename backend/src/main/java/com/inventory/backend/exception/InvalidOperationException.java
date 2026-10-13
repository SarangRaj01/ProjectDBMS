package com.inventory.backend.exception;

/**
 * Thrown when an operation violates a domain or business rule (e.g. invalid status transition).
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
