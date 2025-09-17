package com.kpi.fict.aura.auth.dto;

import java.util.Set;

public record RoleDto(
        String name,
        Set<String> permissions) {}
