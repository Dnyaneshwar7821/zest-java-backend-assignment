/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secretKey = "dGhpc0lzQVZlcnlTZWN1cmVTZWNyZXRLZXlGb3JKd3RUb2tlbkdlbmVyYXRpb25aZXN0QXNzaWdubWVudDEyMzQ1Njc4OTAxMjM0NTY=";
    private final long expirationMs = 3600000;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", expirationMs);
    }

    @Test
    @DisplayName("Should generate a valid JWT token and parse username correctly")
    void testGenerateAndParseToken() {
        String username = "testuser";
        List<String> roles = List.of("ROLE_USER");

        String token = jwtTokenProvider.generateTokenFromUsername(username, roles);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo(username);
    }

    @Test
    @DisplayName("Should return false when validating an invalid JWT token")
    void testValidateInvalidToken() {
        String invalidToken = "invalid.token.structure";
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }
}
