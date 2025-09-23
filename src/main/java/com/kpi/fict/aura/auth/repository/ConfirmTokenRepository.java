package com.kpi.fict.aura.auth.repository;

import com.kpi.fict.aura.auth.dto.ConfirmTokenType;
import com.kpi.fict.aura.auth.model.ConfirmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfirmTokenRepository extends JpaRepository<ConfirmToken, Long> {

    Optional<ConfirmToken> findByTokenAndTokenType(String token, ConfirmTokenType type);

    Optional<ConfirmToken> findByCredentialsIdAndTokenType(Long credentialsId, ConfirmTokenType type);

    Optional<ConfirmToken> findByCredentialsIdAndCodeAndTokenType(Long credentialsId, String code, ConfirmTokenType type);

}