package com.kpi.fict.aura.auth.dto;

public record UserCredentialsDto(
        Long userId,
        Long credentialsId,
        String username,
        String email,
        boolean enabled) {}