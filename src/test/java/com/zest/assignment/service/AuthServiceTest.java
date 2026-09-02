/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.service;

import com.zest.assignment.dto.request.LoginRequest;
import com.zest.assignment.dto.request.RefreshTokenRequest;
import com.zest.assignment.dto.request.RegisterRequest;
import com.zest.assignment.dto.response.AuthResponse;
import com.zest.assignment.dto.response.TokenRefreshResponse;
import com.zest.assignment.entity.RefreshToken;
import com.zest.assignment.entity.Role;
import com.zest.assignment.entity.User;
import com.zest.assignment.exception.DuplicateResourceException;
import com.zest.assignment.repository.UserRepository;
import com.zest.assignment.security.JwtTokenProvider;
import com.zest.assignment.security.UserDetailsImpl;
import com.zest.assignment.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AsyncAuditService asyncAuditService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("encoded_pass")
                .roles(Set.of(Role.ROLE_USER))
                .build();

        refreshToken = RefreshToken.builder()
                .id(100L)
                .token("sample-refresh-token-uuid")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    @DisplayName("Register User - Success")
    void testRegisterUser_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("Secret@123")
                .build();

        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateTokenFromUsername(eq("john_doe"), any())).thenReturn("jwt.token.mock");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);
        when(jwtTokenProvider.getExpirationTimeMs()).thenReturn(900000L);

        AuthResponse response = authService.registerUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("john_doe");
        assertThat(response.getAccessToken()).isEqualTo("jwt.token.mock");
        assertThat(response.getRefreshToken()).isEqualTo("sample-refresh-token-uuid");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register User - Duplicate Username Throws DuplicateResourceException")
    void testRegisterUser_DuplicateUsername_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("Secret@123")
                .build();

        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    @DisplayName("Authenticate User - Success")
    void testAuthenticateUser_Success() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("john_doe")
                .password("Secret@123")
                .build();

        Authentication auth = mock(Authentication.class);
        UserDetailsImpl userPrincipal = UserDetailsImpl.build(user);
        when(auth.getPrincipal()).thenReturn(userPrincipal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("jwt.token.mock");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(refreshToken);
        when(jwtTokenProvider.getExpirationTimeMs()).thenReturn(900000L);

        AuthResponse response = authService.authenticateUser(request);

        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("john_doe");
        assertThat(response.getAccessToken()).isEqualTo("jwt.token.mock");
    }

    @Test
    @DisplayName("Refresh Token - Rotation Success")
    void testRefreshToken_Success() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("old-token")
                .build();

        RefreshToken newRefreshToken = RefreshToken.builder()
                .id(101L)
                .token("new-rotated-token")
                .user(user)
                .build();

        when(refreshTokenService.rotateRefreshToken("old-token")).thenReturn(newRefreshToken);
        when(jwtTokenProvider.generateTokenFromUsername(eq("john_doe"), any())).thenReturn("new.jwt.token");
        when(jwtTokenProvider.getExpirationTimeMs()).thenReturn(900000L);

        TokenRefreshResponse response = authService.refreshToken(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new.jwt.token");
        assertThat(response.getRefreshToken()).isEqualTo("new-rotated-token");
    }

    @Test
    @DisplayName("Logout User - Success")
    void testLogoutUser_Success() {
        doNothing().when(refreshTokenService).revokeToken("valid-token");

        authService.logoutUser("valid-token");

        verify(refreshTokenService, times(1)).revokeToken("valid-token");
    }
}
