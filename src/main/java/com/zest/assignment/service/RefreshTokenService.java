package com.zest.assignment.service;

import com.zest.assignment.entity.RefreshToken;

import java.util.Optional;

public interface RefreshTokenService {

    Optional<RefreshToken> findByToken(String token);

    RefreshToken createRefreshToken(Long userId);

    RefreshToken verifyExpiration(RefreshToken token);

    RefreshToken rotateRefreshToken(String requestRefreshToken);

    int deleteByUserId(Long userId);

    void revokeToken(String token);
}
