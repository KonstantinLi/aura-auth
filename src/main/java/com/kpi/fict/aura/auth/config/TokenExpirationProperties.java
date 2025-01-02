package com.kpi.fict.aura.auth.config;

import com.kpi.fict.aura.auth.dto.TokenScope;
import com.kpi.fict.aura.auth.exception.TokenScopeDataNotInitializedException;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "application.security.token")
public record TokenExpirationProperties(List<TokenExpirationDto> expiration) {

    public record TokenExpirationDto(TokenScope scope, Long access, Long refresh) {}

    public TokenExpirationDto byScope(TokenScope scope) {
        return expiration.stream().filter(token -> token.scope() == scope).findAny().orElseThrow(
                () -> new TokenScopeDataNotInitializedException(scope));
    }

}