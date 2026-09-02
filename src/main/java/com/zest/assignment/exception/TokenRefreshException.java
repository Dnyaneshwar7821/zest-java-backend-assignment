/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.exception;

public class TokenRefreshException extends RuntimeException {
    public TokenRefreshException(String token, String message) {
        super(String.format("Failed for [%s]: %s", token, message));
    }
}
