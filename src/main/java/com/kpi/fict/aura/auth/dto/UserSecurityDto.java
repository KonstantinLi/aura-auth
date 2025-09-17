package com.kpi.fict.aura.auth.dto;

import java.util.Set;

public record UserSecurityDto(
        Long id,
        String username,
        String email,
        String password,
        Long credentialsId,
        boolean enabled,
        boolean registered,
        Set<RoleDto> roles) {}