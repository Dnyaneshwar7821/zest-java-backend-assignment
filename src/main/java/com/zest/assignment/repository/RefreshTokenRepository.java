/**
 * Zest India IT Assessment - Production-Grade RESTful API
 */
package com.zest.assignment.repository;

import com.zest.assignment.entity.RefreshToken;
import com.zest.assignment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    @Modifying
    int deleteByUser(User user);

    @Modifying
    int deleteByToken(String token);
}
