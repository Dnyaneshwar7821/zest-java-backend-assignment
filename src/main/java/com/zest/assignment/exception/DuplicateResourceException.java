/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
