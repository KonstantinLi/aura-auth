package com.kpi.fict.aura.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthTokensResponse(
        Long userId,
        String token,
        String refreshToken,
        AuthDetails details) {}