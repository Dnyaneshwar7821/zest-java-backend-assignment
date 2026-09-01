package com.zest.assignment.service;

import com.zest.assignment.dto.request.LoginRequest;
import com.zest.assignment.dto.request.RefreshTokenRequest;
import com.zest.assignment.dto.request.RegisterRequest;
import com.zest.assignment.dto.response.AuthResponse;
import com.zest.assignment.dto.response.TokenRefreshResponse;

public interface AuthService {

    AuthResponse registerUser(RegisterRequest request);

    AuthResponse authenticateUser(LoginRequest request);

    TokenRefreshResponse refreshToken(RefreshTokenRequest request);

    void logoutUser(String refreshToken);
}
