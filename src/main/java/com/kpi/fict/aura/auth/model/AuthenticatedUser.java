package com.kpi.fict.aura.auth.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Collections;

@Getter
public class AuthenticatedUser extends User {

    private static final String IGNORED_PASSWORD = "";

    private final Long id;

    private final Long credentialsId;

    public AuthenticatedUser(Long id, Long credentialsId, String username, String password, boolean enabled) {
        super(username, password == null ? IGNORED_PASSWORD : password,
                enabled, true, true, true, Collections.emptyList());
        this.id = id;
        this.credentialsId = credentialsId;
    }

}
