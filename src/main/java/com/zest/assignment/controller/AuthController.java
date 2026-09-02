/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.controller;

import com.zest.assignment.dto.request.LoginRequest;
import com.zest.assignment.dto.request.RefreshTokenRequest;
import com.zest.assignment.dto.request.RegisterRequest;
import com.zest.assignment.dto.response.ApiResponse;
import com.zest.assignment.dto.response.AuthResponse;
import com.zest.assignment.dto.response.MessageResponse;
import com.zest.assignment.dto.response.TokenRefreshResponse;
import com.zest.assignment.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, authentication, token refresh rotation, and logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Register a new user account", description = "Creates a new user profile with default or specified roles and returns initial JWT tokens.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User successfully registered"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error in request body"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Username or email already in use")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.registerUser(request);
        return new ResponseEntity<>(
                ApiResponse.success(authResponse, "User registered successfully"),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Authenticate user credentials", description = "Validates username/email and password, returning a JWT access token and refresh token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User authenticated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials provided")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.authenticateUser(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "User authenticated successfully"));
    }

    @PostMapping("/refresh-token")
    @SecurityRequirements
    @Operation(summary = "Refresh JWT Access Token using Token Rotation", description = "Validates the refresh token, revokes it, and issues a new access token along with a new rotated refresh token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed and rotated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Refresh token expired, revoked, or invalid")
    })
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse tokenResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(tokenResponse, "Access token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out user", description = "Revokes the active refresh token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User logged out successfully")
    })
    public ResponseEntity<ApiResponse<MessageResponse>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authService.logoutUser(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success(
                MessageResponse.builder().message("Logged out successfully").build(),
                "Logged out successfully"
        ));
    }
}
