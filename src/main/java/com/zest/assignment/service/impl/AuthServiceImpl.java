package com.zest.assignment.service.impl;

import com.zest.assignment.dto.request.LoginRequest;
import com.zest.assignment.dto.request.RefreshTokenRequest;
import com.zest.assignment.dto.request.RegisterRequest;
import com.zest.assignment.dto.response.AuthResponse;
import com.zest.assignment.dto.response.TokenRefreshResponse;
import com.zest.assignment.entity.RefreshToken;
import com.zest.assignment.entity.Role;
import com.zest.assignment.entity.User;
import com.zest.assignment.exception.DuplicateResourceException;
import com.zest.assignment.exception.ResourceNotFoundException;
import com.zest.assignment.repository.UserRepository;
import com.zest.assignment.security.JwtTokenProvider;
import com.zest.assignment.security.UserDetailsImpl;
import com.zest.assignment.service.AsyncAuditService;
import com.zest.assignment.service.AuthService;
import com.zest.assignment.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AsyncAuditService asyncAuditService;

    @Override
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' is already taken!");
        }

        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' is already registered!");
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            roles.add(Role.ROLE_USER);
        } else {
            roles.addAll(request.getRoles());
        }

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        List<String> roleNames = savedUser.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toList());

        String jwt = jwtTokenProvider.generateTokenFromUsername(savedUser.getUsername(), roleNames);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());

        asyncAuditService.logAudit(
                "USER_REGISTERED",
                "User",
                savedUser.getId(),
                savedUser.getUsername(),
                "User account registered with roles: " + roleNames
        );

        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(new HashSet<>(roleNames))
                .expiresIn(jwtTokenProvider.getExpirationTimeMs())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse authenticateUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail().trim(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtTokenProvider.generateToken(authentication);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());

        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        asyncAuditService.logAudit(
                "USER_LOGIN",
                "User",
                userPrincipal.getId(),
                userPrincipal.getUsername(),
                "User successfully authenticated"
        );

        return AuthResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .roles(roles)
                .expiresIn(jwtTokenProvider.getExpirationTimeMs())
                .build();
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken rotatedRefreshToken = refreshTokenService.rotateRefreshToken(request.getRefreshToken());
        User user = rotatedRefreshToken.getUser();

        List<String> roleNames = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toList());

        String newAccessToken = jwtTokenProvider.generateTokenFromUsername(user.getUsername(), roleNames);

        asyncAuditService.logAudit(
                "TOKEN_REFRESHED",
                "RefreshToken",
                rotatedRefreshToken.getId(),
                user.getUsername(),
                "Access token refreshed and refresh token rotated"
        );

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(rotatedRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTimeMs())
                .build();
    }

    @Override
    @Transactional
    public void logoutUser(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revokeToken(refreshToken);
            asyncAuditService.logAudit(
                    "USER_LOGOUT",
                    "RefreshToken",
                    null,
                    "ANONYMOUS",
                    "Refresh token revoked upon logout"
            );
        }
    }
}
