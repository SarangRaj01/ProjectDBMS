package com.inventory.backend.exception;

/**
 * Thrown when attempting to create or update a resource with duplicate unique attributes (e.g. SKU).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
