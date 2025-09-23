package com.kpi.fict.aura.auth.repository;

import com.kpi.fict.aura.auth.dto.TokenScope;
import com.kpi.fict.aura.auth.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    void deleteByUserId(Long userId);

    Optional<Token> findByRefreshToken(String refreshToken);

    Optional<Token> findByUserIdAndScope(Long userId, TokenScope scope);

}