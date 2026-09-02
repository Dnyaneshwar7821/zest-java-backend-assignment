/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.exception;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
