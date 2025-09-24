package com.kpi.fict.aura.auth.exception;

import com.kpi.fict.aura.auth.dto.ConfirmTokenType;

public class TokenExpiredException extends RuntimeException {

    private static final String MESSAGE_TEMPLATE = "%s %s expired";

    public TokenExpiredException(ConfirmTokenType tokenType, boolean code) {
        super(MESSAGE_TEMPLATE.formatted(tokenType, code ? "code" : "token"));
    }

}
