package com.kpi.fict.aura.auth.exception;

import com.kpi.fict.aura.auth.dto.TokenScope;

public class TokenScopeDataNotInitializedException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "%s token data not initialized";

    public TokenScopeDataNotInitializedException(TokenScope scope) {
        super(MESSAGE_TEMPLATE.formatted(scope));
    }

}
