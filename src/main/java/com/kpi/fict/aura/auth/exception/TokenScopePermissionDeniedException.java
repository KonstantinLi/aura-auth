package com.kpi.fict.aura.auth.exception;

import com.kpi.fict.aura.auth.dto.TokenScope;

public class TokenScopePermissionDeniedException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "%s token is not allowed for this endpoint";

    public TokenScopePermissionDeniedException(TokenScope scope) {
        super(MESSAGE_TEMPLATE.formatted(scope));
    }

}
