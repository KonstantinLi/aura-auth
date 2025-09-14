package com.kpi.fict.aura.auth.service;

import com.kpi.fict.aura.auth.config.TokenExpirationProperties;
import com.kpi.fict.aura.auth.dto.TokenScope;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

import static com.kpi.fict.aura.auth.config.AuraAuthenticationConfiguration.AUTHORIZATION_TOKEN_PREFIX;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final TokenExpirationProperties tokenExpiration;

    public JwtService(TokenExpirationProperties tokenExpiration,
            @Value("${application.security.token-secret-key}") String tokenSecretKey) {
        this.tokenExpiration = tokenExpiration;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(tokenSecretKey));
    }

    public String generateToken(Long authenticatedUserId, TokenScope scope) {
        return createToken(authenticatedUserId, scope, tokenExpiration.byScope(scope).access());
    }

    public String generateRefreshToken(Long authenticatedUserId, TokenScope scope) {
        return createToken(authenticatedUserId, scope, tokenExpiration.byScope(scope).refresh());
    }

    public boolean isAuthorizationHeaderValueNotAcceptable(String authorizationHeaderValue) {
        if (authorizationHeaderValue == null || !authorizationHeaderValue.startsWith(AUTHORIZATION_TOKEN_PREFIX)) {
            return true;
        }
        return isTokenNotAcceptable(extractTokenFromHeaderValue(authorizationHeaderValue));
    }

    public boolean isTokenNotAcceptable(String token) {
        try {
            Claims jwtClaims = extractAllClaims(token);
            return jwtClaims.getExpiration().toInstant().isBefore(Instant.now()) || jwtClaims.getSubject() == null;
        } catch (JwtException | IllegalArgumentException ex) {
            return true;
        }
    }

    public String extractSubject(String authorizationHeaderValue) {
        return extractAllClaims(extractTokenFromHeaderValue(authorizationHeaderValue)).getSubject();
    }

    public String extractClaim(String authorizationHeaderValue, String claim) {
        return extractAllClaims(extractTokenFromHeaderValue(authorizationHeaderValue)).get(claim, String.class);
    }

    private String createToken(Long authenticatedUserId, TokenScope scope, Long tokenExpirationMilliseconds) {
        if (tokenExpirationMilliseconds == null) return null;
        return Jwts.builder()
                .subject(authenticatedUserId.toString())
                .claim("scope", scope)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(tokenExpirationMilliseconds)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String extractTokenFromHeaderValue(String headerValue) {
        return headerValue.substring(AUTHORIZATION_TOKEN_PREFIX.length());
    }

}