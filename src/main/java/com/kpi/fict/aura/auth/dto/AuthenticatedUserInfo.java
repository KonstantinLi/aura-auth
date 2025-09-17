package com.kpi.fict.aura.auth.dto;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record AuthenticatedUserInfo(
        Long id,
        Collection<? extends GrantedAuthority> authorities) {}