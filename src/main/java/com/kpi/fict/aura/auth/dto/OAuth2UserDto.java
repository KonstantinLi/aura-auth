package com.kpi.fict.aura.auth.dto;

public record OAuth2UserDto(
        String firstName,
        String lastName,
        String email,
        byte[] image,
        SignUpMethod method) {}